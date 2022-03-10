package run;

import split.NLPSentenceSplit;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NLPMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) {
        String input = allLinesOneString("res/happy.txt");
        System.out.println(input);
        NLPSentenceSplit nlpSplit = new NLPSentenceSplit();
        List<String> split = nlpSplit.splitText(input);
        if(DEBUG)split.forEach(s -> System.out.println(s));
        
    }

    public static String allLinesOneString(String srcContentrootPath) {
        Scanner sc = FileUtils.readFile(srcContentrootPath);
        String allLines = "";
        while(sc.hasNextLine()) {
            String nextLine = sc.nextLine();
            allLines += " " + nextLine;
        }
        sc.close();

        return allLines;
    }
}
