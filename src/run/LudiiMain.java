package run;

import model.nlp_case.NGramModel;
import split.NLPSentenceSplit;
import utils.FileUtils;
import utils.ReadAllGameFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LudiiMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) {
        //gather all lud files
        ArrayList<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        String input = allLinesOneString(locations.get(0));
        NGramModel m = new NGramModel(input, 2);
        locations = (ArrayList<String>) locations.subList(1, locations.size() - 1);
        for(String path : locations) {
            input = allLinesOneString(path);
            List<String> split = NLPSentenceSplit.splitText(input);
            m.addToModel(split);
        }

        List<String> picklist = m.getPicklist(NLPSentenceSplit.splitText("Bobba fat loves spending time with my family and"), 5);
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
