import morfessor
import nltk

class RunMorfessor():
    u"""Training and decoding using Morfessor"""
    
    def __init__(self, unsuper_file, semisuper_file):
        self.io = morfessor.MorfessorIO()

        # build models
        unsupervised = morfessor.baseline.BaselineModel()
        compounds = list(self.io.read_corpus_file(unsuper_file))
        self.unsupervised.load_data(compounds)

        semisupervised = morfessor.baseline.BaselineModel()
        annotations = list(self.io.read_annotations_file(semisuper_file))
        semisupervised.load_data(compounds)
        self.semisupervised.set_annotations(annotations)
    
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
            print(self.unsupervised.viterbi_segment(token))



def evaluate(self,gold_data,models):
    gold = self.io.read_annotations_file(gold_data)
    ev = morfessor.MorfessorEvaluation(gold)

    for m in models:
        ev.evaluate_model(m)

if __name__ == '__main__':
    hansard = RunMorfessor('data/train/NunavutHansard-text','data/train/NunavutHansard-annotation')

    hansard.train_batch_recursive()
    hansard.read_testset('data/test/exodus-text')