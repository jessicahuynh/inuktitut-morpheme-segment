package scripts;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ProcessBuilder;

public class Corpus {

    public String path;
    public ArrayList<String> text;
    
    public Corpus(String path, Boolean cleaned) throws FileNotFoundException, IOException {
        this.path = path;
        
        try {
            ArrayList<String> lines = null;
            // if corpus isn't already all text
            // should only be for Nunavut Hansard
            if (!cleaned) {
                lines = makeCorpusOnlyInuktitut(path);
            }
            else {
                // read corpus into lines
                lines = readInInuktitutText(path);
            }

            // tokenize corpus

            this.text = lines;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static ArrayList<String> readInInuktitutText(String path) throws FileNotFoundException, IOException {

        ArrayList<String> inuktitutCorpus = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

        String line = null;
        while ((line = reader.readLine()) != null) {
            inuktitutCorpus.add(line);
        }

        return inuktitutCorpus;

    }

    private static ArrayList<String> makeCorpusOnlyInuktitut(String path) throws FileNotFoundException, IOException {

        ArrayList<String> cleanCorpus = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("*")) {
                String inuktitut = reader.readLine();
                cleanCorpus.add(inuktitut);
            }
        }

        return cleanCorpus;

    }

    private void tokenizeCorpus() {
        ArrayList<String> tokenizedCorpus = new ArrayList<String>();
        
        ArrayList<String> currentCorpus = this.text;
        for (String line : currentCorpus) {
            String tempLine = "";
            String[] words = line.split("[\\p{IsPunctuation}\\p{IsWhite_Space}]");
            for (String w : words) {
                tempLine += cleanWord(w);
            }
            if (tempLine.length() > 1) {
                tokenizedCorpus.add(tempLine.trim());
            }
        }

        this.text = tokenizedCorpus;
    }

    private String cleanWord(String word) {
        String cleanedWord = word;

        if (word.length() > 2) {
            if (word.contains(">")) {
                cleanedWord = cleanedWord.replaceAll(">","");
            }
            if (word.contains("<")) {
                cleanedWord = cleanedWord.replaceAll("<","");
            }
            if (word.contains(",")) {
                cleanedWord = cleanedWord.replaceAll(",","");
            }
            if (word.contains("$")) {
                cleanedWord = cleanedWord.replaceAll("$","");
            }
            if (word.contains("&")) {
                cleanedWord = cleanedWord.replaceAll("&","%26");
            }
            if (word.contains("5")) {
                cleanedWord = word.replaceAll("5","");
            }

            // check if it's a number
            if (word.matches("^-?\\d+$")) {
                cleanedWord = "";
            }
        }
        else {
            cleanedWord = "";
        }

        return cleanedWord.trim() + " ";
    }

    public void splitTrainTest(String typeModel, String dir) throws IOException {
        String testDir = dir+"/test/";
        String trainDir = dir+"/train/";

        String fileName = "";
        if (this.path.contains("/")) {
            String[] arr =  this.path.split(".txt")[0].split("/");
            fileName = arr[arr.length - 1];
        }
        else {
            String[] arr = this.path.split(".txt")[0].split("\\\\");
            fileName = arr[arr.length - 1];
        }
        
        switch (typeModel) {
            case "text":
                buildTextCorpus(testDir,trainDir,fileName+"-text");
                break;
            case "annotation":
                buildAnnotationCorpus(trainDir,fileName+"-annotation");
                buildAnnotationCorpus(testDir,fileName+"-gold"); // gold standard
                break;
        }
    }

    private void buildTextCorpus(String testDir, String trainDir, String fileName) throws IOException {
        int numLines = this.text.size();
        
        int numTestLines = numLines / 5;
        if (numLines <= 5) {
            numTestLines = 1;
        }
        
        // build sets in arraylists
        ArrayList<String> test = new ArrayList<String>(numTestLines);
        ArrayList<String> train = new ArrayList<String>(numLines - numTestLines);
        for (int i = 0; i < numLines; i++) {
            String currentLine = this.text.get(i);
            if (i <= numTestLines) {
                test.add(currentLine);
            }
            else {
                train.add(currentLine);
            }
        }
        
        createTrainTest(train,test,trainDir+fileName,testDir+fileName);
    }

    private void buildAnnotationCorpus(String trainDir, String fileName) throws IOException {
        int numLines = this.text.size();

        int numTestLines = numLines / 5;
        if (numLines <= 5) {
            numTestLines = 1;
        }

        int j = 0;
        int corpSize = 0;
        if (trainDir.contains("test")) {
            j = 0;
            corpSize = numTestLines;
        }
        else {
            j = numTestLines;
            corpSize = numLines - 2;
        }

        // get types instead of tokens
        // use HashSet for annotations to avoid multiple of the same morpheme segmentation
        HashMap<String,HashSet<String>> wordAndAnnotations = new HashMap<String,HashSet<String>>();
        for (int i = j; i <= corpSize; i++) {
            String[] currentLine = this.text.get(i).split("\\s+");
            for (String w : currentLine) {
                HashSet<String> annotations = getMorphemesFromJar(w);
                wordAndAnnotations.put(w,annotations);
            }
        }

        // properly format lines for Morfessor annotation corpus
        ArrayList<String> train = new ArrayList<String>();
        for (String w : wordAndAnnotations.keySet()) {
            HashSet<String> annotations = wordAndAnnotations.get(w);
            if (annotations.size() > 0) {
                if (annotations.size() == 1) {
                    String h = "";
                    for (String a : annotations) {
                        h += a;
                    }
                    train.add(w + " " + h);
                }
                else {
                    String hypotheses = "";
                    for (String a : annotations) {
                        hypotheses += a + ", ";
                    }
                    hypotheses = hypotheses.substring(0,hypotheses.length() - 2);

                    train.add(w + " " + hypotheses);
                }
            }
        }
        
        createTrain(train,trainDir+fileName);
    }

    private HashSet<String> getMorphemesFromJar(String word) throws IOException {
        HashSet<String> hypotheses = new HashSet<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            String command = "java -jar ./Uqailaut.jar " + word;
            Process proc = rt.exec(command);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s = null;
            HashSet<String> output = new HashSet<String>();
            
            while ((s = stdInput.readLine()) != null) {
                output.add(s);
            }
            if (!output.isEmpty()) {
                Pattern m = Pattern.compile("\\{\\w+:");
                for (String o : output) {
                    Matcher matcher = m.matcher(o);
                    String hypothesis = "";

                    while (matcher.find()) {
                        String morpheme = matcher.group();
                        morpheme = morpheme.substring(1,morpheme.length()-1);
                        hypothesis += morpheme + " ";
                    }
                    hypothesis = hypothesis.substring(0,hypothesis.length() - 1);

                    hypotheses.add(hypothesis);
                }
            }
            else {
                return output;
            }

        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return hypotheses;
    }

    public static void createTrain(ArrayList<String> train, String trainPath) throws IOException {
        try {
            // train
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainPath));
            for (String line : train) {
                trainWriter.write(line + "\n");
            }
            trainWriter.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTrainTest(ArrayList<String> train, ArrayList<String> test, String trainPath, String testPath) throws IOException {
        try {
            createTrain(train, trainPath);

            // test
            BufferedWriter testWriter = new BufferedWriter(new FileWriter(testPath));
            for (String line : test) {
                testWriter.write(line + "\n");
            }
            testWriter.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("Pass in some arguments!");
        }
        else {
            // create Corpus objects
            HashMap<String,Corpus> corpora = new HashMap<String,Corpus>();
            corpora.put(args[1], new Corpus(args[1],false)); // Nunavut Hansard

            // bible corpora
            if (args[2] != null) {
                File bibleDir = new File(args[2]);
                for (File bibleChapter : bibleDir.listFiles()) {
                    corpora.put(bibleChapter.toString(),new Corpus(bibleChapter.toString(),true));
                }
            }

            // tokenize and create text to later build models
            for (Corpus corpus : corpora.values()) {
                corpus.tokenizeCorpus();
                corpus.splitTrainTest("text",args[0]);
                corpus.splitTrainTest("annotation",args[0]);
            }
        }
    }
}