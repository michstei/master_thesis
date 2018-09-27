package keras.features;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import utils.KerasCSVReader;

public interface KerasFeature extends GlobalFeature, KerasExtractor {
    enum DistanceFunction{
        DISTANCEFUNCTION_COSINE,
        DISTANCEFUNCTION_L1,
        DISTANCEFUNCTION_L2,
        DISTANCEFUNCTION_JSD,
        DISTANCEFUNCTION_CHISQUARE,
        DISTANCEFUNCTION_KSDIST,
        DISTANCEFUNCTION_SIMPLEEMD,
        DISTANCEFUNCTION_TANIMOTO
        }

    /***
     * caalculate distance betrween 2 featurevectors
     * @param featureVectorSelf  featurevector 1
     * @param featureVectorOther featurevector 2
     * @param usedDistanceFun    distancefunction to use
     * @return  Distance based on usedDistanceFun
     */
    static double getDistance(double[] featureVectorSelf,double[] featureVectorOther, DistanceFunction usedDistanceFun ) {
        switch (usedDistanceFun) {
            case DISTANCEFUNCTION_COSINE: {
                return MetricsUtils.cosineCoefficient(featureVectorSelf, featureVectorOther);
            }
            case DISTANCEFUNCTION_L1: {
                return MetricsUtils.distL1(featureVectorSelf,featureVectorOther);
            }
            case DISTANCEFUNCTION_L2: {
                return MetricsUtils.distL2(featureVectorSelf,featureVectorOther);
            }
            case DISTANCEFUNCTION_JSD: {
                return MetricsUtils.jsd(featureVectorSelf,featureVectorOther);
            }
            case DISTANCEFUNCTION_CHISQUARE: {
                return MetricsUtils.chisquare(featureVectorSelf,featureVectorOther);
            }
            case DISTANCEFUNCTION_KSDIST: {
                return MetricsUtils.ksDistance(featureVectorSelf,featureVectorOther);
            }
            case DISTANCEFUNCTION_SIMPLEEMD: {
                return MetricsUtils.simpleEMD(featureVectorSelf,featureVectorOther);
            }
            case DISTANCEFUNCTION_TANIMOTO: {
                return MetricsUtils.cosineCoefficient(featureVectorSelf, featureVectorOther);
            }
            default:
                return MetricsUtils.cosineCoefficient(featureVectorSelf, featureVectorOther);
        }
    }


    /***
     * initializes the reader with the csvFilename (if name in reader equals csvFilename, reader is returned unchanged)
     * @param csvFilename   path to the csv file
     * @param reader        reader object or null
     * @return  reader 
     */
    static KerasCSVReader setCsvFilename(String csvFilename, KerasCSVReader reader) {

        if(csvFilename != null ) {
            if (reader == null || !csvFilename.equals((reader.getFilepath()))) {
                // get featureVector from csv file
                reader = new KerasCSVReader(csvFilename, ",");
            }

        }
        return reader;
    }


}
