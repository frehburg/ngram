package interfaces;

import java.util.List;

/**
 *
 * @param <G> class of words
 */
public interface iNGramInstance<G> {
    List<G> getInstance();
    int getMultiplicity();

    void increaseMultiplicity();
    boolean isEqual(iNGramInstance<G> otherInstance);
    boolean isWordEqual(G w1, G w2);
}
