package run;

import gzip.GZIPCompression;
import gzip.GZIPDecompression;
import model.Ludii.NGramModelLudii;
import split.LudiiFileCleanup;
import utils.FileUtils;
import utils.Pair;
import utils.ReadAllGameFiles;
import utils.Triple;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LudiiMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) throws IOException {
        FileWriter fw = FileUtils.writeFile("res/Compression2/compressionComplexity.csv");
        fw.write("N,compression_ms,compression_s,compression_min,decompression_ms,decompression_s,decompression_min\n");
        for(int N = 2; N <= 17; N++) {
            String compressedPath = "res/Compression2/compressed/CompressedLudiiModel"+N+".gz";
            long startDecompression = System.currentTimeMillis();
            NGramModelLudii m = decompressRead(compressedPath);
            long finishDecompression = System.currentTimeMillis();
            long decompressionTime = finishDecompression - startDecompression;
            String outputPath = "res/Compression2/CompressedLudiiModel"+N+".gz";
            long startCompression = System.currentTimeMillis();
            compressWrite(m,outputPath);
            long finishCompression = System.currentTimeMillis();
            long compressionTime = finishCompression - startCompression;
            fw.write(N+","+compressionTime+","+(double)compressionTime/1000+","
                    +(double)compressionTime/(1000*60)+","+decompressionTime+","
                    +(double)decompressionTime/1000+","+(double)decompressionTime/(1000*60)+"\n");
        }
        fw.close();
    }

    public static void compressWrite(NGramModelLudii m, String path) {
        String tmpPath = "res/tmp/tmp.csv";
        m.writeModel(tmpPath);
        GZIPCompression.compress(tmpPath,path);
        FileUtils.deleteFile(tmpPath);
    }

    public static NGramModelLudii decompressRead(String path) {
        String tmpDirectory = "res/tmp/tmp.csv";
        GZIPDecompression.decompress(path,tmpDirectory);
        NGramModelLudii m = NGramModelLudii.readModel(tmpDirectory);
        FileUtils.deleteFile(tmpDirectory);
        return m;
    }

    public static void createAndWriteModels(int maxN) {
        List<Triple<Integer,Long,Long>> timeComplexity = new ArrayList<>();
        for(int N = 2; N <= 20; N++) {
            long start = System.currentTimeMillis();
            String compressedPath = createAndWriteModel(N);
            long finish = System.currentTimeMillis();
            long computationTime = finish - start;
            long bytes = 0;
            try {
                bytes = Files.size(Paths.get(compressedPath));
                timeComplexity.add(new Triple<>(N,computationTime,bytes));
                FileWriter fw = FileUtils.writeFile("res/Compression2/timecomplexity.csv");
                fw.write("N,Time(ms),bytes\n");
                for(Triple<Integer,Long,Long> t : timeComplexity) {
                    fw.write(t.getR()+","+t.getS()+","+t.getT()+"\n");
                }
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String createAndWriteModel(int N) {
        //gather all lud files
        List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        String input = LudiiFileCleanup.allLinesOneString(locations.get(0));
        NGramModelLudii m = new NGramModelLudii(input,N);
        locations = locations.subList(1, locations.size());
        for(String s : locations) {
            m.addToModel(LudiiFileCleanup.allLinesOneString(s));
        }
        String path = "res/Compression2/LudiiModel"+N+".csv";
        m.writeModel(path);
        String compressedPath = "res/Compression2/CompressedLudiiModel"+N+".gz";
        GZIPCompression.compress(path,compressedPath);
        List<String> context = Arrays.asList(new String[]{"("});
        int i = 1;
        for(String rec : m.getPicklist(context)) {
            System.out.println(i+". "+rec);
            i++;
        }
        return compressedPath;
    }
}
