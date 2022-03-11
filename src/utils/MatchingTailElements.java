package utils;

import java.util.List;

public final class MatchingTailElements {
    /**
     * Returns the number of matching elements starting at the end of the list.
     * E.g.1: Same last element
     * --> get(superlist=[1,52,6,2,9],sublist=[1,2,3,4,5,6,7,8,9]) = 1
     * E.g.2: Same first 5 elements but different last element
     * --> get(superlist=[1,2,3,4,5],sublist=[1,2,3,4,5,6]) = 0
     * E.g.3: Same first 4 elements but different last element
     * --> get(superlist=[1,2,3,4,5],sublist=[1,2,3,4,6]) = 0
     * E.g.4: Same last 3 elements
     * --> get(superlist=[1,2,3,4,5],sublist=[3,4,5]) = 3
     * E.g.5: sublist is a sublist of superlist, but not at the end
     * --> get(superlist=[0,1,2,3,0],sublist=[1,2,3]) = 0
     * E.g.6: sublist is longer than superlist, but the last 3 elements match
     * --> get(superlist=[3,4,5],sublist=[1,2,3,4,5]) = 3
     * @param superlist
     * @param sublist
     * @param <F>
     * @return
     */
    public static <F> int count(List<F> superlist, List<F> sublist) {
        //0. if one of them is empty, return false
        if(superlist.isEmpty() || sublist.isEmpty())
            return 0;
        //2. Starting at the back, compare the elements of both lists
        int i;
        for(i = 1; i < sublist.size() && i < superlist.size(); i++) {
            F superCur = superlist.get(superlist.size() - i), subCur = sublist.get(sublist.size() - i);
            //check if is NOT the same and return one less than i
            if(!superCur.equals(subCur)) {
                return i - 1;
            }
        }
        return i;
    }
}
