package run;

import model.Ludii.NGramModelLudii;
import split.NLPSentenceSplit;
import utils.FileUtils;
import utils.ReadAllGameFiles;

import java.util.List;
import java.util.Scanner;

public class LudiiMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) {
        //gather all lud files
        List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        String input = allLinesOneString(locations.get(0));
        NGramModelLudii m = new NGramModelLudii(input, 2);
        locations = locations.subList(1, locations.size() - 1);
        for(String path : locations) {
            System.out.println("Adding game: " + path);
            input = allLinesOneString(path);
            List<String> split = NLPSentenceSplit.splitText(input);
            m.addToModel(split);
        }

        m.writeModel("res/Ludii2Model.csv");
        NGramModelLudii m1 = NGramModelLudii.readModel("res/Ludii2Model.csv");
        m1.writeModel("res/Ludii2Model - Copy.csv");
        /*List<String> picklist = m.getPicklist(NLPSentenceSplit.splitText("Bobba fat loves spending time with my family and"), 5);
        if(DEBUG)System.out.println("PICKLIST: ");
        if(DEBUG)picklist.forEach(s -> System.out.println(s));*/
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