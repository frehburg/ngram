package interfaces;

import java.util.List;

public interface iSentenceSplit<H,G> {
    List<G> splitText(H text);
}
