package utils;

import model.Ludii.NGramInstanceLudii;

import java.util.List;

public class MultiplicityBucketSortComparator implements BucketSortComparator<Pair<NGramInstanceLudii,Integer>>{
    @Override
    public int getKey(Pair<NGramInstanceLudii, Integer> p) {
        int multiplicity = p.getR().getMultiplicity();
        return multiplicity;
    }
}
