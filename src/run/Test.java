package run;

import split.LudiiFileCleanup;
import split.NLPSentenceSplit;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        String s = LudiiFileCleanup.allLinesOneString("C:\\Users\\filre\\OneDrive\\Documents\\IntelliJ\\ngram\\res\\Ludii\\lud\\board\\space\\line\\Tic-Tac-Toe.lud");
        List<String> l = NLPSentenceSplit.splitText(s);
        System.out.println(l);
        l = LudiiFileCleanup.cleanup(l);
        System.out.println(l);
    }
}
