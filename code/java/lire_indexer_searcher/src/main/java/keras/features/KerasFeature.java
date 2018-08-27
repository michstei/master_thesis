package keras.features;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;

public interface KerasFeature extends GlobalFeature, KerasExtractor {
    enum DistanceFunction{
        DISTANCE_COSINE,
        DISTANCE_L1,
        DISTANCE_L2,
        DISTANCE_JSD,
        DISTANCE_CHISQUARE,
        DISTANCE_KSDIST,
        DISTANCE_SIMPLEEMD,
        DISTANCE_TANIMOTO
        }
    static double getDistance(LireFeature lf, DistanceFunction usedDistanceFun, double[] featureVector) {
        switch (usedDistanceFun) {
            case DISTANCE_COSINE: {
                return MetricsUtils.cosineCoefficient(featureVector, lf.getFeatureVector());
            }
            case DISTANCE_L1: {
                return MetricsUtils.distL1(featureVector,lf.getFeatureVector());
            }
            case DISTANCE_L2: {
                return MetricsUtils.distL2(featureVector,lf.getFeatureVector());
            }
            case DISTANCE_JSD: {
                return MetricsUtils.jsd(featureVector,lf.getFeatureVector());
            }
            case DISTANCE_CHISQUARE: {
                return MetricsUtils.chisquare(featureVector,lf.getFeatureVector());
            }
            case DISTANCE_KSDIST: {
                return MetricsUtils.ksDistance(featureVector,lf.getFeatureVector());
            }
            case DISTANCE_SIMPLEEMD: {
                return MetricsUtils.simpleEMD(featureVector,lf.getFeatureVector());
            }
            case DISTANCE_TANIMOTO: {
                return MetricsUtils.cosineCoefficient(featureVector, lf.getFeatureVector());
            }
            default:
                return MetricsUtils.cosineCoefficient(featureVector, lf.getFeatureVector());
        }
    }




}
