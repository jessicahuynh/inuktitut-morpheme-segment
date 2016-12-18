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
            case "wordlist":
                buildWordListCorpus();
                break;
            case "annotation":
                buildAnnotationCorpus();
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

    private void buildWordListCorpus() throws IOException {

    }

    private void buildAnnotationCorpus() throws IOException {

    }

    public static void createTrainTest(ArrayList<String> train, ArrayList<String> test, String trainPath, String testPath) throws IOException {
        try {
            // train
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainPath));
            for (String line : train) {
                trainWriter.write(line + "\n");
            }
            trainWriter.close();

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
            corpora.put(args[0], new Corpus(args[0],false));

            // bible corpora
            if (args[1] != null) {
                File bibleDir = new File(args[1]);
                for (File bibleChapter : bibleDir.listFiles()) {
                    corpora.put(bibleChapter.toString(),new Corpus(bibleChapter.toString(),true));
                }
            }

            // tokenize and build models
            for (Corpus corpus : corpora.values()) {
                corpus.tokenizeCorpus();
                corpus.splitTrainTest("text",args[2]);
            }
        }
    }
}