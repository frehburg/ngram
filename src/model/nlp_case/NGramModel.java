package model.nlp_case;

import interfaces.iNGramInstance;
import interfaces.iNGramModel;
import split.NLPSentenceSplit;
import utils.MatchingTailElements;
import utils.Pair;

import java.util.*;

/**
 * In this case the key is the second to last word, to predict the next one
 * possible bcs at least taking 2-grams
 */
public class NGramModel implements iNGramModel<List<String>,String> {
    public static boolean DEBUG = true;
    private final String text;
    private final int N;
    private Comparator<NGramInstance> instanceComparator;
    private HashMap<String, List<NGramInstance>> dictionary;
    private NLPSentenceSplit nlpSplit;

    public NGramModel(String text, int N) {
        this.text = text;
        this.N = N;
        dictionary = new HashMap<>();
        nlpSplit = new NLPSentenceSplit();
        instanceComparator = new Comparator<NGramInstance>() {
            @Override
            public int compare(NGramInstance o1, NGramInstance o2) {
                return o2.getMultiplicity() - o1.getMultiplicity();
            }
        };
        if(DEBUG)System.out.println(text);
        List<String> split = NLPSentenceSplit.splitText(text);
        if(DEBUG)split.forEach(s -> System.out.println(s));
        constructModel(split, N);
    }
    @Override
    public void constructModel(List<String> text, int N) {
        // n is the length of the created n grams
        createNGramInstances(text, true);
        //TODO: calculate the overall multiplicities of the key for each instance
        System.out.println("Model:"+toString());
    }

    public List<NGramInstance> createNGramInstances(List<String> split, boolean addToDictionary) {
        ArrayList<NGramInstance> nGramInstances = new ArrayList<>();
        for(int n = N; n > 1; n--) {
            // go through the list of all words and create n grams of specified length
            System.out.println("********************************");
            System.out.println("N="+n);
            for(int i = 0; i+n <= split.size(); i++) {
                //the i+n in this line requires it in the loop above
                List<String> currentWords = split.subList(i,i+n);
                System.out.println("-----------------------------------");
                System.out.println(currentWords);
                // create a new ngram instance here
                NGramInstance current = new NGramInstance(currentWords);
                //insert it
                nGramInstances.add(current);
                if(addToDictionary) {
                    insert(current);
                }
            }
        }
        return nGramInstances;
    }

    @Override
    public List<String> getPicklist(List<String> context) {
        //this first fetches the picklist
        String key = context.get(context.size() - 1);
        List<NGramInstance> picklist = dictionary.getOrDefault(key,new ArrayList<>());
        if(picklist.isEmpty()) {
            //no predictions found
            return new ArrayList<>();
        }
        //this list also stores the amount of matching words at the end of the ngram with the context
        List<Pair<NGramInstance,Integer>> matchingCountPicklist = new ArrayList<>();
        for(NGramInstance instance : picklist) {
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
        HashMap<String,List<Pair<NGramInstance,Integer>>> predictionMatch = new HashMap<>();
        for(Pair<NGramInstance,Integer> p : matchingCountPicklist) {
            String pKey = p.getR().getLast();
            List<Pair<NGramInstance,Integer>> samePredictionAsP;
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
        Set<Map.Entry<String, List<Pair<NGramInstance, Integer>>>> pmEntrySet = predictionMatch.entrySet();
        // wipe this clean to rewrite to it with united instances
        matchingCountPicklist = new ArrayList<>();
        for(Map.Entry<String, List<Pair<NGramInstance, Integer>>> entry : pmEntrySet) {
            int pMultiplicity = 0;
            int maxMatchingWords = 0;
            String entryPrediction = entry.getKey();
            List<String> newInstanceWords = Arrays.asList(new String[]{key,entryPrediction});
            for(Pair<NGramInstance,Integer> p : entry.getValue()) {
                pMultiplicity += p.getR().getMultiplicity();
                //if p has more matching words than stored, update, else do nothing
                maxMatchingWords = p.getS() > maxMatchingWords ? p.getS() : maxMatchingWords;
            }
            matchingCountPicklist.add(new Pair<>(new NGramInstance(newInstanceWords,pMultiplicity), maxMatchingWords));
        }
        //sort the picklist
        matchingCountPicklist.sort(new Comparator<Pair<NGramInstance, Integer>>() {
            @Override
            public int compare(Pair<NGramInstance, Integer> p1, Pair<NGramInstance, Integer> p2) {
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
        for(Pair<NGramInstance,Integer> p : matchingCountPicklist) {
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

    @Override
    /**
     * Strategy:
     * -if ngram already exists, increase multiplicity
     * -if ngram does not exist, insert ngram
     *
     * The method keeps a list of the ngrams with the same key as instance: instancesWithSameKey
     */
    public void insert(iNGramInstance<List<String>,String> instance) {
        NGramInstance convertedInstance = (NGramInstance) instance;

        String wsKey = convertedInstance.getKey();
        if(dictionary.containsKey(wsKey)) {
            List<NGramInstance> instancesWithSameKey = dictionary.get(wsKey);
            //there can be multiple ngrams for one key
            boolean foundEqualInstance = false;
            for(NGramInstance nGramInstance : instancesWithSameKey) {
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
            List<NGramInstance> instancesWithSameKey = new ArrayList<>();
            instancesWithSameKey.add(convertedInstance);
            dictionary.put(wsKey, instancesWithSameKey);
        }
    }

    public String toString() {
        Set<Map.Entry<String, List<NGramInstance>>> entryset = dictionary.entrySet();
        ArrayList<Map.Entry<String, List<NGramInstance>>> entries = new ArrayList<>();
        entries.addAll(entryset);
        entries.sort(new Comparator<Map.Entry<String, List<NGramInstance>>>() {
            @Override
            public int compare(Map.Entry<String, List<NGramInstance>> o1, Map.Entry<String, List<NGramInstance>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        String output = "";
        for(Map.Entry<String, List<NGramInstance>> entry : entries) {
            output += entry.getKey() + "->" + entry.getValue() + "\n";
        }
        return output;
    }

}
