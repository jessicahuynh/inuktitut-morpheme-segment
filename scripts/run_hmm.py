import morfessor
import nltk
import sys

class RunMorfessor():
    u"""Training and decoding using Morfessor"""
    
    unsupervised = None
    semisupervised = None
    tokens = []
    io = None
    
    def __init__(self, unsuper_file, semisuper_file,acw):
        self.io = morfessor.MorfessorIO()

        # build models
        self.unsupervised = self.io.read_binary_model_file(unsuper_file)

        self.semisupervised = self.io.read_binary_model_file(unsuper_file)
        annotations = dict(self.io.read_annotations_file(semisuper_file))
        self.semisupervised.set_annotations(annotations,acw)
    
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
        for token in self.tokens:
            print(self.unsupervised.viterbi_segment(token)[0])



def evaluate(self,gold_data,morf):
    gold = self.io.read_annotations_file(gold_data)
    ev = morfessor.MorfessorEvaluation(gold)

    models = [morf.unsupervised, morf.semisupervised]

    for m in models:
        ev.evaluate_model(m)

if __name__ == '__main__':
    m = RunMorfessor(sys.argv[1],sys.argv[2],0.0)

    m.train_batch_recursive()
    m.read_testset(sys.argv[3])
    m.decode_unsupervised()

    evaluate(sys.argv[4],m)