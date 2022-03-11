package interfaces;

import java.util.List;

/**
 * Key = last word in ngram, value = ngram
 * @param <H> class of sentences
 * @param <G> class of words
 */
public interface iNGramModel<H, G> {
    void constructModel(H text);
    void addToModel(H text);
    Object getPicklist(H context);
    Object getPicklist(H context, int maxLength);
    void writeModel(String path);
    //static iNGramModel<H,G> readModel(String path);
}
