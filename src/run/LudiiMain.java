package run;

import model.Ludii.NGramModelLudii;
import split.LudiiFileCleanup;
import split.NLPSentenceSplit;
import utils.FileUtils;
import utils.ReadAllGameFiles;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class LudiiMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) throws IOException {
        //gather all lud files
        List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        String input = LudiiFileCleanup.allLinesOneString(locations.get(0));
        locations = locations.subList(1, locations.size() - 1);
        for(String path : locations) {
            input = LudiiFileCleanup.allLinesOneString(path);
            List<String> split = NLPSentenceSplit.splitText(input);
            System.out.println("---------------------------------------");
            System.out.println("Contains metadata: "+split.contains("metadata")+ " contains comments: " + split.contains("//"));
            System.out.println("+++++++++++++++++");
            split = LudiiFileCleanup.cleanup(split);
            System.out.println("Contains metadata: "+split.contains("metadata")+ " contains comments: " + split.contains("//"));
            FileWriter fw = FileUtils.writeFile(path+"1.txt");
            fw.write(split.toString());
            fw.close();
        }
    }
}
