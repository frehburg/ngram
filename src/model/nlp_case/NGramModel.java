package model.nlp_case;

import interfaces.iNGramInstance;
import interfaces.iNGramModel;
import split.NLPSentenceSplit;

import java.util.*;

/**
 * In this case the key is the second to last word, to predict the next one
 * possible bcs at least taking 2-grams
 */
public class NGramModel implements iNGramModel<String,String> {
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
        constructModel(text, N);
    }
    @Override
    public void constructModel(String text, int N) {
        if(DEBUG)System.out.println(text);
        List<String> split = nlpSplit.splitText(text);
        if(DEBUG)split.forEach(s -> System.out.println(s));

        // n is the length of the created n grams
        createNGramInstances(split, true);
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

    /*public List<NGramInstance> createContextNGramInstances(List<String> contextSplit) {
        List<NGramInstance> contextNGramInstances = new ArrayList<>();
        for(int n = N; n > 1; n--) {
            // go through the list of all words and create n grams of specified length
            int endSubList = contextSplit.size()-1;
            int startSubList = endSubList-n;
            if(startSubList < 0)
                continue;
            List<String> currentWords = contextSplit.subList(startSubList,endSubList);
            // create a new ngram instance here
            NGramInstance current = new NGramInstance(currentWords);
            //insert it
            contextNGramInstances.add(current);
        }
        return contextNGramInstances;
    }*/

    @Override
    public List<String> getPicklist(String context) {
        //TODO add checking of whole background, not just last word
        List<String> contextSplit = nlpSplit.splitText(context);
        String key = contextSplit.get(contextSplit.size() - 1);
        List<NGramInstance> picklist = dictionary.get(key);
        picklist.sort(instanceComparator);
        ArrayList<String> stringPicklist = new ArrayList<>();
        for(NGramInstance instance : picklist) {
            //TODO: use tuple with rec and multiplicity as return; for now append multiplicity to string
            stringPicklist.add(instance.getLast() + " Multiplicity: " + instance.getMultiplicity());
        }
        //TODO: combine same recommendations into one
        if(DEBUG)System.out.println("FULL PICKLIST: ");
        if(DEBUG)picklist.forEach(s -> System.out.println(s));
        return stringPicklist;
    }

    @Override
    public List<String> getPicklist(String context, int maxLength) {
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
    public void insert(iNGramInstance<String> instance) {
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
