package kerasfeatures;

import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

public interface KerasFeature extends LireFeature, Extractor {
    void extract(String imageFilename);

}
