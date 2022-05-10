package utils;

import model.Ludii.NGramInstanceLudii;

public class MatchingWordsBucketSortComparator implements BucketSortComparator<Pair<NGramInstanceLudii,Integer>>{
    @Override
    public int getKey(Pair<NGramInstanceLudii, Integer> p) {
        int matchingWords = p.getS();
        return matchingWords;
    }
}
