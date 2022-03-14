package run;

import gzip.GZIPCompression;
import model.Ludii.NGramModelLudii;
import split.LudiiFileCleanup;
import utils.ReadAllGameFiles;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LudiiMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) throws IOException {
        createAndWriteModel(3);
    }

    public static void createAndWriteModel(int N) {
        //gather all lud files
        List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        String input = LudiiFileCleanup.allLinesOneString(locations.get(0));
        NGramModelLudii m = new NGramModelLudii(input,N);
        locations = locations.subList(1, locations.size());
        for(String s : locations) {
            m.addToModel(LudiiFileCleanup.allLinesOneString(s));
        }
        m.writeModel("res/Compression2/LudiiModel"+N+".csv");
        GZIPCompression.compress("res/Compression2/LudiiModel"+N+".csv","res/Compression2/CompressedLudiiModel"+N+".gz");
        List<String> context = Arrays.asList(new String[]{"("});
        int i = 1;
        for(String rec : m.getPicklist(context)) {
            System.out.println(i+". "+rec);
            i++;
        }
    }
}
