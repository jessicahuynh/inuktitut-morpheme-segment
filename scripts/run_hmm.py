import morfessor
import nltk
import sys

class RunMorfessor():
    u"""Training and decoding using Morfessor"""
    
    unsupervised = None
    semisupervised = None
    tokens = []
    io = None
    acw = 0
    segs = None
    
    def __init__(self, unsuper_file, semisuper_file,acw):
        self.io = morfessor.MorfessorIO()
        self.acw = acw

        # build models
        self.unsupervised = self.io.read_binary_model_file(unsuper_file)

        self.semisupervised = self.io.read_binary_model_file(unsuper_file)
        a = self.io.read_annotations_file(semisuper_file)
        annotations = {}
        for word in a:
            hypotheses = []
            for hypothesis in a[word]:
                h = ""
                for morpheme in hypothesis:
                    h += morpheme + " "
                hypotheses.append(h[:-1])
            annotations[word] = hypotheses
        self.semisupervised.set_annotations(annotations,acw)

        # write segmentation
        with open('models/bible-segmentation','w') as f:
            for word in annotations:
                construction = ""
                for hypothesis in annotations[word]:
                    construction += hypothesis + ", "
                construction = construction[:-2]
                f.write(construction+"\n")
        self.segs = self.io.read_segmentation_file('models/bible-segmentation',has_counts=False)
    
    def get_segs(self):
        return self.segs
    
    def train_batch_recursive(self):
        """batch training on given model files with recursive algorithm"""

        self.unsupervised.train_batch(algorithm='recursive')
        self.semisupervised.train_batch(algorithm='recursive')       

    def read_testset(self,test_file):
        all_tokens = []
        with open(test_file,'r') as f:
            for sentence in f:
                tokens = nltk.word_tokenize(sentence)
                all_tokens.extend(tokens)

        self.tokens = all_tokens
    
    def decode_unsupervised(self):
        with open('results/result_segs_unsupervised','w') as f1:
            for token in self.tokens:
                morphemes = self.unsupervised.viterbi_segment(token)[0]
                decomposed = ""
                for morpheme in morphemes:
                    decomposed += morpheme + "+"
                decomposed = decomposed[:-1]
                f1.write(token+": "+decomposed+"\n")

        with open('results/result_segs_semi_'+str(self.acw),'w') as f2:
            f2.write(str(self.acw)+"\n-----------------\n")
            for token in self.tokens:
                morphemes = self.unsupervised.viterbi_segment(token)[0]
                decomposed = ""
                for morpheme in morphemes:
                    decomposed += morpheme + "+"
                decomposed = decomposed[:-1]
                f2.write(token+": "+decomposed+"\n")
            f2.write("\n")


def evaluate(gold_data,morf,acw,segs):
    print(str(acw) + " weighted annotated corpus")
    io = morfessor.MorfessorIO()
    gold = io.read_annotations_file(gold_data)
    ev = morfessor.MorfessorEvaluation(gold)

    models = [morf.unsupervised, morf.semisupervised]

    # evaluate models
    config = morfessor.evaluation.EvaluationConfig(10,25)
    results = [ev.evaluate_model(m,config) for m in models]
    print("model evaluation")
    print(results[0])
    print(results[1])
    
    wsr = morfessor.evaluation.WilcoxonSignedRank()
    r = wsr.significance_test(results)
    wsr.print_table(r)

    # evaluate segmentations
    # config2 = morfessor.evaluation.EvaluationConfig(5,15)
    # eval_seg = [ev.evaluate_segmentation(segs,config2) for m in models]
    # print("segmentation evaluation")
    # print(eval_seg[0])
    # print(eval_seg[1])

    # wsr = morfessor.evaluation.WilcoxonSignedRank()
    # r_seg = wsr.significance_test(eval_seg)
    # wsr.print_table(r_seg)

if __name__ == '__main__':
    weights = [0.0, 0.1, 0.25, 0.5, 0.75, 0.95, 1.0, 2.0, 2.5, 5.0, 7.5, 10.0]

    for w in weights:
        m = RunMorfessor(sys.argv[1],sys.argv[2],w)

        m.train_batch_recursive()
        m.read_testset(sys.argv[3])
        m.decode_unsupervised()

        evaluate(sys.argv[4],m,w,m.get_segs())