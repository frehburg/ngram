package utils;

import model.Ludii.NGramInstanceLudii;

import java.util.*;

public class Sorter {
    private static final boolean DEBUG = false;

    /**
     * This list fisrt uses a bucketsort to sort all recommendations after the number of occurrences
     * It the filters out the the top entries, specified in the limit parameter
     * Then it uses a second becket sort for every previous bucket to sort them by the number of occurrences
     * @param matchingCountPicklist
     * @param limit
     * @return
     */
    public static  List<Pair<NGramInstanceLudii,Integer>> nestedBucketSort(List<Pair<NGramInstanceLudii,Integer>> matchingCountPicklist, int limit) {
        //use a bucket sort to sort after matched words
        //this is how each pair is: matchingCountPicklist.add(new Pair<>(new NGramInstanceLudii(newInstanceWords,pMultiplicity), maxMatchingWords));
        List<List<Pair<NGramInstanceLudii,Integer>>> firstBucketsSortedList = bucketSort(matchingCountPicklist, new MatchingWordsBucketSortComparator());
        //count up the first limit items and discard of the rest, this method also sorts the sublists
        List<Pair<NGramInstanceLudii,Integer>> sortedList = reduceSubListItems(firstBucketsSortedList, limit);
        // displays the list for debugging
        if(true) {
            for(int i = 0; i < sortedList.size(); i++) {
                Pair<NGramInstanceLudii,Integer> p = sortedList.get(i);
                NGramInstanceLudii instance = p.getR();
                String recommendation = instance.getKey();
                int matchingWords = p.getS();
                System.out.println((i+1) + ". " + recommendation + " multiplicity: " + instance.getMultiplicity() + " matching words: " + matchingWords);
            }
        }
        return sortedList;
    }

    public static  List<Pair<NGramInstanceLudii,Integer>> ravelList(List<List<Pair<NGramInstanceLudii,Integer>>> list2d) {
        List<Pair<NGramInstanceLudii,Integer>> ravelledList = new ArrayList<>();
        for(List<Pair<NGramInstanceLudii,Integer>> bucket : list2d) {
            for(Pair<NGramInstanceLudii,Integer> p : bucket) {
                ravelledList.add(p);
            }
        }
        return ravelledList;
    }

    /**
     * This sorts the list after some criterion provided by the comparator into buckets,
     * and then empties the buckets into the list in the correct order
     * @param list
     * @return
     */
    public static List<List<Pair<NGramInstanceLudii,Integer>>> bucketSort(List<Pair<NGramInstanceLudii,Integer>> list, BucketSortComparator<Pair<NGramInstanceLudii,Integer>> comparator) {
        Map<Integer,List<Pair<NGramInstanceLudii,Integer>>> firstBuckets = new HashMap<>();
        for(Pair<NGramInstanceLudii,Integer> p : list) {
            int key = comparator.getKey(p);
            List<Pair<NGramInstanceLudii,Integer>> curBucket = firstBuckets.getOrDefault(key, new ArrayList<>());
            curBucket.add(p);
            firstBuckets.put(key,curBucket);
        }
        //count the number of elements in each bucket
        // first is the key from firstBuckets and then the bucketsize
        List<Pair<Integer,Integer>> firstBucketSizes = new ArrayList<>();
        for(Map.Entry<Integer,List<Pair<NGramInstanceLudii,Integer>>> entry : firstBuckets.entrySet()) {
            int bucketSize = entry.getValue().size();
            firstBucketSizes.add(new Pair<>(entry.getKey(), bucketSize));
        }
        firstBucketSizes.sort(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                //R is the size of the bucket
                return o2.getR() - o1.getR();
            }
        });
        //create a new list that contains the lists presorted after matchingwords descendingly
        List<List<Pair<NGramInstanceLudii,Integer>>> firstBucketsSortedList = new ArrayList<>();
        for(int i = 0; i < firstBucketSizes.size(); i++) {
            int curMatchinWords = firstBucketSizes.get(i).getR();
            List<Pair<NGramInstanceLudii,Integer>> curBucket = firstBuckets.get(curMatchinWords);
            firstBucketsSortedList.add(curBucket);
            if(DEBUG)System.out.println("Matching words: " + curMatchinWords + " List of instances: " + curBucket);
        }
        return firstBucketsSortedList;
    }

    /**
     * This method removes any sublist items past the limit, or leaves everything intact if there are less than limit items
     * @param limit
     * @return
     */
    public static List<Pair<NGramInstanceLudii,Integer>> reduceSubListItems(List<List<Pair<NGramInstanceLudii,Integer>>> list, int limit) {
        int amountOfItems = 0;
        List<Pair<NGramInstanceLudii,Integer>> picklist = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            List<Pair<NGramInstanceLudii,Integer>> curSubList = list.get(i);
            //first need to sort sublist by multiplicity
            List<List<Pair<NGramInstanceLudii,Integer>>> curSubListSorted = bucketSort(curSubList,new MultiplicityBucketSortComparator());
            // ravel the sorted list
            curSubList = ravelList(curSubListSorted);

            //case 1: the recommendations in the current sublist are not over the limit
            if(amountOfItems + curSubList.size() <= limit) {
                picklist.addAll(curSubList);
                amountOfItems += curSubList.size();
                continue;
            }
            //case 2: the recommendations in the current sublist are over the limit
            else {
                // calculate how many recs are still allowed
                int allowed = limit - amountOfItems;
                // the first allowed items of the curSubList are still allowed
                List<Pair<NGramInstanceLudii,Integer>> curSubListAllowed = curSubList.subList(0,allowed);
                picklist.addAll(curSubListAllowed);
                break;
            }
        }
        return picklist;
    }

    /**
     * Standard sort used before
     * @param matchingCountPicklist
     * @return
     */
    public static  List<Pair<NGramInstanceLudii,Integer>> defaultSort(List<Pair<NGramInstanceLudii,Integer>> matchingCountPicklist) {
        matchingCountPicklist.sort(new Comparator<Pair<NGramInstanceLudii, Integer>>() {
            @Override
            public int compare(Pair<NGramInstanceLudii, Integer> p1, Pair<NGramInstanceLudii, Integer> p2) {
                int matchingWords1 = p1.getS(), matchingWords2 = p2.getS();
                int diffMatchingwords = matchingWords2 - matchingWords1;
                if(diffMatchingwords == 0) {
                    //need to check multiplicity, since both have the same multiplicity
                    int multiplicity1 = p1.getR().getMultiplicity(), multiplicity2 = p2.getR().getMultiplicity();
                    int diffMultiplicity = multiplicity2 - multiplicity1;
                    return diffMultiplicity;
                } else {
                    return diffMatchingwords;
                }
            }
        });
        return matchingCountPicklist;
    }
}
