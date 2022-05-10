package run;

import model.Ludii.NGramInstanceLudii;
import split.LudiiFileCleanup;
import split.SentenceSplit;
import utils.Pair;
import utils.Sorter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        List<Pair<NGramInstanceLudii, Integer>> list = new ArrayList<>();
        for(int i = 0; i < 60; i++) {
            ArrayList<String> a = new ArrayList<>();
            a.add("Word"+i);
            a.add("Abc");
            Random r = new Random();
            int matchingWords =  r.nextInt(7);
            int multiplicity = r.nextInt(2000);
            //System.out.println("i "+i+" matching "+ matchingWords);
            list.add(new Pair<>(new NGramInstanceLudii(a,multiplicity),matchingWords));
        }

        Sorter.nestedBucketSort(list,15);
    }
}
