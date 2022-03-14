package split;

import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LudiiFileCleanup {

    public static List<String> cleanup(List<String> split) {
        List<String> cleanedUpSplit = new ArrayList<>();
        boolean foundMetadata = false;
        for(String w : split) {
            //check for metadata
            if(w.equals("metadata")) {
                foundMetadata = true;
                break;
            }
            //check for comments
            if(!w.contains("//") /* && //TODO: check for weird tab symbol*/) {
                cleanedUpSplit.add(w);
            }
        }
        if(foundMetadata) {
            cleanedUpSplit = cleanedUpSplit.subList(0,cleanedUpSplit.size()-1);
        }
        return cleanedUpSplit;
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
