package split;

import interfaces.iSentenceSplit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NLPSentenceSplit implements iSentenceSplit<String,String> {
    @Override
    /**
     * This method will split a text of natural language into its words and
     * symbols.
     * It will split at all spaces and punctuation.
     */
    public List<String> splitText(String text) {
        List<String> split = new ArrayList<>();
        String currentWord = "";
        for(int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            switch(currentChar) {
                case ' ':
                    split.add(currentWord);
                    currentWord = "";
                    break;
                case '(':
                case ')':
                case ',':
                case '.':
                case '!':
                case '?':
                case ';':
                    split.add(currentWord);
                    currentWord = "";
                    String symbol = ""+currentChar;
                    split.add(symbol);
                    break;
                default:
                    currentWord += currentChar;
                    break;
            }
        }
        return split;
    }
}
