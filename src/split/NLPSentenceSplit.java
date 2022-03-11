package split;

import java.util.ArrayList;
import java.util.List;

public final class NLPSentenceSplit {
    /**
     * This method will split a text of natural language into its words and
     * symbols.
     * It will split at all spaces and punctuation.
     */
    public static List<String> splitText(String text) {
        List<String> split = new ArrayList<>();
        String currentWord = "";
        for(int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            switch(currentChar) {
                case ' ':
                    if(!currentWord.equals(""))split.add(currentWord);
                    currentWord = "";
                    break;
                case '(':
                case ')':
                case ',':
                case '.':
                case '!':
                case '?':
                case ';':
                case '{':
                case '}':
                case '[':
                case ']':
                case '<':
                case '>':
                    if(!currentWord.equals(""))split.add(currentWord);
                    currentWord = "";
                    String symbol = ""+currentChar;
                    split.add(symbol);
                    break;
                case '\n':break;
                default:
                    currentWord += currentChar;
                    break;
            }
        }
        if(!currentWord.equals(""))split.add(currentWord);
        return split;
    }
}
