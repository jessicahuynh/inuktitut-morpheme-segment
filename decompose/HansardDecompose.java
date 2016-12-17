package decompose;

import applications.Decompose;
//import donnees.DonneesLinguistiquesAbstract;
import java.util.*;
import java.io.*;

public class HansardDecompose {
    /**
    * Gets Inuktitut words from Hansard corpus
    *
    * @param   corpus  directory path to the Hansard
    * @return          HashMap from word to empty array
    */
    public static HashMap<String, String[]> getHansardWords(String corpus) throws FileNotFoundException, IOException {
        HashMap<String, String[]> inuktitutWords = new HashMap<String,String[]>();

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

                        if (word.length() > 2 && !word.contains("$") && !word.matches(".*\\d+.*") && word != "-") {
                            // encode for analyzer later
                            if (word.contains("&")) {
                                word = word.replaceAll("&","%26");
                            }
                            
                            if (word.contains("/")) {
                                String[] wordPair = word.split("/");
                                for (String w : wordPair) {
                                    inuktitutWords.put(w,new String[0]);
                                }
                            }
                            else {
                                inuktitutWords.put(word,new String[0]);
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
    public static HashMap<String,String[]> decomposeHansard(HashMap<String,String[]> map) {
        
        return map;
    }

    /**
    * Print word and decomposition to file
    * 
    * @param   map HashMap from word to array of morphemes
    * @return      void
    */
    public static void printDecomposition(HashMap<String,String[]> map) {

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("Pass in the path to the Nunavut Hansard corpus!");
        }
        else {
            HashMap<String,String[]> wordMap = getHansardWords(args[0]);
        }
    }

}