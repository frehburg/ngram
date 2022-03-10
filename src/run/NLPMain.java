package run;

import model.nlp_case.NGramInstance;
import model.nlp_case.NGramModel;
import split.NLPSentenceSplit;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NLPMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) {
        String input = allLinesOneString("res/happy.txt");
        NGramModel m = new NGramModel(input, 5);
        List<String> picklist = m.getPicklist("if", 5);
        if(DEBUG)System.out.println("PICKLIST: ");
        if(DEBUG)picklist.forEach(s -> System.out.println(s));
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
