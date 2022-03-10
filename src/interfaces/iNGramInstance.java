package interfaces;

import java.util.List;

/**
 *
 * @param <G> class of words
 */
public interface iNGramInstance<G> {
    List<G> getWords();
    int getMultiplicity();
    G getKey();
    G getLast();
    void increaseMultiplicity();
    boolean equals(iNGramInstance<G> otherInstance);
    boolean isWordEqual(G w1, G w2);
    String toString();
}
