package decompose;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ProcessBuilder;

public class HansardDecompose {
    /**
    * Gets Inuktitut words from Hansard corpus
    *
    * @param   corpus  directory path to the Hansard
    * @return          HashMap from word to empty array
    */
    public static HashMap<String, ArrayList<String>> getHansardWords(String corpus) throws FileNotFoundException, IOException {
        HashMap<String, ArrayList<String>> inuktitutWords = new HashMap<String,ArrayList<String>>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(corpus)));

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("*")) {
                    String inuktitut = reader.readLine();
                    String[] words = inuktitut.split("\\s+");
                    
                    for (String word : words) {
                        // eliminate punctuation
                        if (word.length() > 2) {
                            if (word.contains(":")) {
                                word = word.split(":")[0];
                            }
                            if (word.contains(";")) {
                                word = word.split(";")[0];
                            }
                            if (word.contains(".")) {
                                word = word.replace(".","");
                            }
                            if (word.contains("\"")) {
                                word = word.replace("\"","");
                            }
                            if (word.contains(",")) {
                                word = word.split(",")[0];
                            }
                            if (word.contains("(")) {
                                word = word.substring(1);
                            }
                            if (word.contains(")")) {
                                word = word.split("\\)")[0];
                            }
                            if (word.contains(".@")) {
                                word = word.split(".@")[0];
                            }
                            if (word.contains("-")) {
                                word = word.replaceAll("[0-9]*-+","");
                            }
                            if (word.contains("<")) {
                                word = word.replace("<","");
                            }
                            if (word.contains(">")) {
                                word = word.replace(">","");
                            }
                            if (word.contains("?")) {
                                word = word.replace("?","");
                            }
                            if (word.contains("’")) {
                                word = word.replace("’","'");
                            }
                            
                        }

                        if (word.length() > 2 && !word.contains("$") && !word.matches(".*\\d+.*") && word != "-" && word != "" && word != " " && word != null) {
                            // encode for analyzer later
                            if (word.contains("&")) {
                                word = word.replaceAll("&","%26");
                            }
                            
                            if (word.contains("/")) {
                                String[] wordPair = word.split("/");
                                for (String w : wordPair) {
                                    inuktitutWords.put(w,new ArrayList<String>());
                                }
                            }
                            else {
                                inuktitutWords.put(word,new ArrayList<String>());
                            }
                        }
                    }
                }
            }

            reader.close();
        }
        catch (Exception e) {
            System.out.println(e.getClass());
        }
        
        return inuktitutWords;
    }

    /**
    * Decomposes Inuktitut words within map
    *
    * @param   map HashMap from word to empty array
    * @return      HashMap from word to array of morphemes
    */
    public static HashMap<String,ArrayList<String>> decomposeHansard(HashMap<String,ArrayList<String>> map)  throws IOException {
        map.remove("");
        System.out.println(map.size());
        try {
            Runtime rt = Runtime.getRuntime();
            for (String word : map.keySet()) {
                String command = "java -jar ./Uqailaut.jar " + word;
                Process proc = rt.exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                String s = null;
                ArrayList<String> output = new ArrayList<String>();
                while ((s = stdInput.readLine()) != null) {
                    output.add(s);
                }
                if (!output.isEmpty()) {
                    map.put(word, getMorphemes(output));
                }

                // print errors to System.out
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

                stdError.close();
                stdInput.close();

            }
        }
        catch (IOException e) {
            System.out.println(e.getClass());
        }
        
        return map;
    }

    /**
     * Returns an array of morphemes for that Inuktitut word
     * 
     * @param   arr array of hypotheses
     * @return      String[] of morphemes
     */
    private static ArrayList<String> getMorphemes(ArrayList<String> arr) {
        String truth = arr.get(0); // take the first hypothesis

        ArrayList<String> morphemes = new ArrayList<String>();

        Matcher matcher = Pattern.compile("\\{\\w+:").matcher(truth);
        while (matcher.find()) {
            String morpheme = matcher.group();
            morphemes.add(morpheme.substring(1,morpheme.length()-1));
        }
        System.out.println(morphemes);
        return morphemes;
    }

    /**
    * Print word and decompositions to file
    * 
    * @param    map         HashMap from word to array of morphemes
    * @param    outputFile  path to where decompositions will be written
    * @return               void
    */
    public static void printDecomposition(HashMap<String,ArrayList<String>> map, String outputFile) throws IOException {
        try {
            BufferedWriter buff = new BufferedWriter(new FileWriter(outputFile));
        
            for (String word : map.keySet()) {
                buff.write(word);

                StringBuilder sb = new StringBuilder();
                for (String morpheme : map.get(word)) {
                    sb.append(morpheme);
                    sb.append("-");
                }
                String segmented = sb.toString();
                buff.write(segmented.substring(0,segmented.length()-1));
                System.out.println(word);
                System.out.println(segmented.substring(0,segmented.length()-1));
            }

            buff.close();
        }
        catch (IOException e) {
            System.out.println(e.getClass());
        }
        
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("Pass in the paths!");
        }
        else {
            long startTime = System.currentTimeMillis();
            
            System.out.println("Getting Inuktitut words");
            HashMap<String,ArrayList<String>> wordMap = getHansardWords(args[0]);
            System.out.println("Decomposing into morphemes");
            HashMap<String,ArrayList<String>> decomposition = decomposeHansard(wordMap);
            System.out.println("Saving morphemes to "+args[1]);
            printDecomposition(decomposition,args[1]);

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(totalTime/1000 + "seconds to decompose the Nunavut Hansard corpus");
        }
    }

}