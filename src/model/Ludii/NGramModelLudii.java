package model.Ludii;

import interfaces.iNGramInstance;
import interfaces.iNGramModel;
import split.LudiiFileCleanup;
import split.NLPSentenceSplit;
import utils.FileUtils;
import utils.MatchingTailElements;
import utils.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * In this case the key is the second to last word, to predict the next one
 * possible bcs at least taking 2-grams
 */
public class NGramModelLudii implements iNGramModel<List<String>,String> {
    public static boolean DEBUG = false;
    private final String text;
    private final int N;
    private Comparator<NGramInstanceLudii> instanceComparator;
    private HashMap<String, List<NGramInstanceLudii>> dictionary;
    private NLPSentenceSplit nlpSplit;

    ///
    private static final String COMMA_REPLACEMENT = "--COMMA--";
    private static final String COMMA = ",";
    private static final String EMPTY_STRING = "";


    /**
     * For creating a new model
     * @param input
     * @param N
     */
    public NGramModelLudii(String input, int N) {
        this.text = input;
        this.N = N;
        this.dictionary = new HashMap<>();
        nlpSplit = new NLPSentenceSplit();
        instanceComparator = new Comparator<NGramInstanceLudii>() {
            @Override
            public int compare(NGramInstanceLudii o1, NGramInstanceLudii o2) {
                return o2.getMultiplicity() - o1.getMultiplicity();
            }
        };
        constructModel(input);
    }

    /**
     * For reading in a model
     * @param dictionary
     * @param text
     * @param N
     */
    private NGramModelLudii(HashMap<String, List<NGramInstanceLudii>> dictionary, String text, int N) {
        this.text = text;
        this.N = N;
        this.dictionary = dictionary;
        nlpSplit = new NLPSentenceSplit();
        instanceComparator = new Comparator<NGramInstanceLudii>() {
            @Override
            public int compare(NGramInstanceLudii o1, NGramInstanceLudii o2) {
                return o2.getMultiplicity() - o1.getMultiplicity();
            }
        };
    }

    @Override
    public void constructModel(String input) {
        addToModel(input);
    }

    @Override
    public void addToModel(String input) {
        if(DEBUG)System.out.println(input);
        List<String> split = NLPSentenceSplit.splitText(input);
        split = LudiiFileCleanup.cleanup(split);
        if(DEBUG)split.forEach(s -> System.out.println(s));
        // n is the length of the created n grams
        createNGramInstances(split, true);
        //TODO: calculate the overall multiplicities of the key for each instance
        if(DEBUG)System.out.println("Model:"+toString());
    }

    public List<NGramInstanceLudii> createNGramInstances(List<String> split, boolean addToDictionary) {
        ArrayList<NGramInstanceLudii> nGramInstances = new ArrayList<>();
        for(int n = N; n > 1; n--) {
            // go through the list of all words and create n grams of specified length
            if(DEBUG)System.out.println("********************************");
            if(DEBUG)System.out.println("N="+n);
            for(int i = 0; i+n <= split.size(); i++) {
                //the i+n in this line requires it in the loop above
                List<String> currentWords = split.subList(i,i+n);
                if(DEBUG)System.out.println("-----------------------------------");
                if(DEBUG)System.out.println(currentWords);
                // create a new ngram instance here
                NGramInstanceLudii current = new NGramInstanceLudii(currentWords);
                //insert it
                nGramInstances.add(current);
                if(addToDictionary) {
                    insertIntoDictionary(current);
                }
            }
        }
        return nGramInstances;
    }

    @Override
    public List<String> getPicklist(List<String> context) {
        //this first fetches the picklist
        String key = context.get(context.size() - 1);
        List<NGramInstanceLudii> picklist = dictionary.getOrDefault(key,new ArrayList<>());
        if(picklist.isEmpty()) {
            //no predictions found
            return new ArrayList<>();
        }
        //this list also stores the amount of matching words at the end of the ngram with the context
        List<Pair<NGramInstanceLudii,Integer>> matchingCountPicklist = new ArrayList<>();
        for(NGramInstanceLudii instance : picklist) {
            List<String> instanceWords = instance.getWords();
            //cut off the prediction before matching to the context
            instanceWords = instanceWords.subList(0,instanceWords.size() - 1);
            int matchingTailElements = MatchingTailElements.count(instanceWords,context);
            if(matchingTailElements > 0) {
                matchingCountPicklist.add(new Pair<>(instance,matchingTailElements));
            }
        }
        //now unite all ngrams with the same prediction into one
        //sort into hashmap by prediction
        HashMap<String,List<Pair<NGramInstanceLudii,Integer>>> predictionMatch = new HashMap<>();
        for(Pair<NGramInstanceLudii,Integer> p : matchingCountPicklist) {
            String pKey = p.getR().getLast();
            List<Pair<NGramInstanceLudii,Integer>> samePredictionAsP;
            if(predictionMatch.containsKey(pKey)) {
                samePredictionAsP = predictionMatch.get(pKey);
                samePredictionAsP.add(p);
            } else {
                samePredictionAsP = new ArrayList<>();
                samePredictionAsP.add(p);
            }
            predictionMatch.put(pKey,samePredictionAsP);
        }
        //for each stored prediction, sum up the multiplicities & take the highest # matching words to create a new prediction
        Set<Map.Entry<String, List<Pair<NGramInstanceLudii, Integer>>>> pmEntrySet = predictionMatch.entrySet();
        // wipe this clean to rewrite to it with united instances
        matchingCountPicklist = new ArrayList<>();
        for(Map.Entry<String, List<Pair<NGramInstanceLudii, Integer>>> entry : pmEntrySet) {
            int pMultiplicity = 0;
            int maxMatchingWords = 0;
            String entryPrediction = entry.getKey();
            List<String> newInstanceWords = Arrays.asList(new String[]{key,entryPrediction});
            for(Pair<NGramInstanceLudii,Integer> p : entry.getValue()) {
                pMultiplicity += p.getR().getMultiplicity();
                //if p has more matching words than stored, update, else do nothing
                maxMatchingWords = p.getS() > maxMatchingWords ? p.getS() : maxMatchingWords;
            }
            matchingCountPicklist.add(new Pair<>(new NGramInstanceLudii(newInstanceWords,pMultiplicity), maxMatchingWords));
        }
        //sort the picklist
        matchingCountPicklist.sort(new Comparator<Pair<NGramInstanceLudii, Integer>>() {
            @Override
            public int compare(Pair<NGramInstanceLudii, Integer> p1, Pair<NGramInstanceLudii, Integer> p2) {
                int multiplicity1 = p1.getR().getMultiplicity(), multiplicity2 = p2.getR().getMultiplicity();
                int diffMultiplicity = multiplicity2 - multiplicity1;
                if(diffMultiplicity == 0) {
                    //need to check matching words, since both have the same multiplicity
                    int matchingWords1 = p1.getS(), matchingWords2 = p2.getS();
                    int diffMatchingwords = matchingWords2 - matchingWords1;
                    return diffMatchingwords;
                } else {
                    return diffMultiplicity;
                }
            }
        });
        ArrayList<String> stringPicklist = new ArrayList<>();
        for(Pair<NGramInstanceLudii,Integer> p : matchingCountPicklist) {
            //TODO: use tuple with rec and multiplicity as return; for now append multiplicity to string
            stringPicklist.add("Prediction: \""+p.getR().getLast() + "\", Multiplicity: " + p.getR().getMultiplicity() + " & Matching words w/ context: " + p.getS());
        }
        if(DEBUG)System.out.println("FULL PICKLIST: ");
        if(DEBUG)picklist.forEach(s -> System.out.println(s));
        return stringPicklist;
    }

    @Override
    public List<String> getPicklist(List<String> context, int maxLength) {
        List<String> fullPicklist = getPicklist(context);
        int picklistSize = fullPicklist.size() - 1;
        //if the list is shorter than maxLength, the whole list is returned, else only a sublist of size maxLength
        return picklistSize < maxLength ? fullPicklist : fullPicklist.subList(0, maxLength);
    }

    /**
     * Strategy:
     * -if ngram already exists, increase multiplicity
     * -if ngram does not exist, insert ngram
     *
     * The method keeps a list of the ngrams with the same key as instance: instancesWithSameKey
     */
    private void insertIntoDictionary(iNGramInstance<List<String>,String> instance) {
        NGramInstanceLudii convertedInstance = (NGramInstanceLudii) instance;

        String wsKey = convertedInstance.getKey();
        if(dictionary.containsKey(wsKey)) {
            List<NGramInstanceLudii> instancesWithSameKey = dictionary.get(wsKey);
            //there can be multiple ngrams for one key
            boolean foundEqualInstance = false;
            for(NGramInstanceLudii nGramInstance : instancesWithSameKey) {
                //check if instance already is in the model
                if(convertedInstance.equals(nGramInstance)) {
                    nGramInstance.increaseMultiplicity();
                    //found equal instance -> cannot find it again
                    foundEqualInstance = true;
                    break;
                }
            }
            if(!foundEqualInstance) {
                //the key exists, but the ngram does not
                instancesWithSameKey.add(convertedInstance);
                dictionary.put(wsKey,instancesWithSameKey);
            }
        } else {
            //the key does not exist
            List<NGramInstanceLudii> instancesWithSameKey = new ArrayList<>();
            instancesWithSameKey.add(convertedInstance);
            dictionary.put(wsKey, instancesWithSameKey);
        }
    }

    @Override
    public void writeModel(String path) {
        Set<Map.Entry<String, List<NGramInstanceLudii>>> dictionaryEntrySet = dictionary.entrySet();
        FileWriter fw = FileUtils.writeFile(path);
        try {
            fw.write(N+"\n");
            fw.write("KEY,WORDS,MULTIPLICITY");
            for(Map.Entry<String, List<NGramInstanceLudii>> entry : dictionaryEntrySet) {
                String key = entry.getKey();
                //csv file splits the strings otherwise
                key = key.replaceAll(COMMA,COMMA_REPLACEMENT);
                boolean first = true;

                //for instances with the same key
                for(NGramInstanceLudii instance : entry.getValue()) {
                    String wordsAsString = EMPTY_STRING;
                    List<String> words = instance.getWords();
                    int i = 0;
                    //for the words of each instance
                    for(String word : words) {
                        wordsAsString += word;
                        if(i < words.size() - 1)
                            wordsAsString += " ";

                        i++;
                    }
                    wordsAsString = wordsAsString.replaceAll(COMMA,COMMA_REPLACEMENT);
                    fw.write("\n"+key+COMMA+wordsAsString+COMMA+instance.getMultiplicity());

                    //makes sure the key is only written on the first occurrence
                    if(first) {
                        first = false;
                        key = EMPTY_STRING;
                    }
                }
            }

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static NGramModelLudii readModel(String path) {
        Scanner sc = FileUtils.readFile(path);
        String nAsString = sc.nextLine();//N as a string
        //remove two commas at the end
        nAsString = nAsString.replaceAll(",,","");
        int N = Integer.parseInt(nAsString);
        sc.nextLine();//to skip the header
        HashMap<String, List<NGramInstanceLudii>> dictionary = new HashMap<>();
        List<NGramInstanceLudii> value = new ArrayList<>();

        //since the csv is compressed, we keep the last string for the next instances
        String lastKey = EMPTY_STRING;

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            System.out.println("--------------------------");
            System.out.println(line);
            String[] split = line.split(COMMA);
            System.out.println("|"+split[0]+"|"+ split[0].equals(EMPTY_STRING));
            String key = split[0];
            key = key.replaceAll(COMMA_REPLACEMENT,COMMA);

            if(key.equals(EMPTY_STRING)) {
                key = lastKey;
            } else if(!key.equals(EMPTY_STRING)) {
                lastKey = key;
            }

            String wordsAsString = split[1];
            String[] wordsAsArray = wordsAsString.split(" ");
            List<String> words = Arrays.asList(wordsAsArray);
            words.forEach(s -> s = s.replaceAll(COMMA_REPLACEMENT,COMMA));
            String multiplicityAsString = split[2];
            int multiplicity = Integer.parseInt(multiplicityAsString);
            if(dictionary.containsKey(key)) {
                value = dictionary.get(key);
            } else {
                value = new ArrayList<>();
            }
            value.add(new NGramInstanceLudii(words,multiplicity));
            dictionary.put(key,value);

        }
        sc.close();
        return new NGramModelLudii(dictionary,"",N);
    }

    public String toString() {
        Set<Map.Entry<String, List<NGramInstanceLudii>>> entryset = dictionary.entrySet();
        ArrayList<Map.Entry<String, List<NGramInstanceLudii>>> entries = new ArrayList<>();
        entries.addAll(entryset);
        entries.sort(new Comparator<Map.Entry<String, List<NGramInstanceLudii>>>() {
            @Override
            public int compare(Map.Entry<String, List<NGramInstanceLudii>> o1, Map.Entry<String, List<NGramInstanceLudii>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        String output = "";
        for(Map.Entry<String, List<NGramInstanceLudii>> entry : entries) {
            output += entry.getKey() + "->" + entry.getValue() + "\n";
        }
        return output;
    }

}
