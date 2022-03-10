package interfaces;

import java.util.List;

/**
 * Key = last word in ngram, value = ngram
 * @param <H> class of sentences
 * @param <G> class of words
 */
public interface iNGramModel<H, G> {
    void constructModel(H text, int n);
    Object getPicklist(H context);
    Object getPicklist(H context, int maxLength);
    void insert(iNGramInstance<G> w);
}
