package interfaces;

/**
 *
 * @param <H> class of sentences
 * @param <G> class of words
 */
public interface iNGramModel<H, G> {
    void constructModel(H text);
    double getProbability(H context, G w);
}
