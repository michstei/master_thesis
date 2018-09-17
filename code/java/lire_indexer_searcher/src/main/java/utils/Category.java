package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Categories of the medicodataset
 */
public enum Category {
    CATEGORY_BLURRY_NOTHING,
    CATEGORY_COLON_CLEAR,
    CATEGORY_DYED_LIFTED_POLYPS,
    CATEGORY_DYED_RESECTION_MARGINS ,
    CATEGORY_ESOPHAGITIS,
    CATEGORY_INSTRUMENTS,
    CATEGORY_NORMAL_CECUM,
    CATEGORY_NORMAL_PYLORUS,
    CATEGORY_NORMAL_Z_LINE,
    CATEGORY_OUT_OF_PATIENT,
    CATEGORY_POLYPS,
    CATEGORY_RETROFLEX_RECTUM,
    CATEGORY_RETROFLEX_STOMACH,
    CATEGORY_STOOL_INCLUSIONS,
    CATEGORY_STOOL_PLENTY,
    CATEGORY_ULCERATIVE_COLITIS;

    /**
     * @return short version of the category
     */
    public String getShortName(){
        switch(this){

            case CATEGORY_BLURRY_NOTHING:
                return "BLN";
            case CATEGORY_COLON_CLEAR:
                return "COC";
            case CATEGORY_DYED_LIFTED_POLYPS:
                return "DLP";
            case CATEGORY_DYED_RESECTION_MARGINS:
                return "DRM";
            case CATEGORY_ESOPHAGITIS:
                return "ESO";
            case CATEGORY_INSTRUMENTS:
                return "INS";
            case CATEGORY_NORMAL_CECUM:
                return "NOC";
            case CATEGORY_NORMAL_PYLORUS:
                return "NOP";
            case CATEGORY_NORMAL_Z_LINE:
                return "NZL";
            case CATEGORY_OUT_OF_PATIENT:
                return "OOP";
            case CATEGORY_POLYPS:
                return "POL";
            case CATEGORY_RETROFLEX_RECTUM:
                return "RER";
            case CATEGORY_RETROFLEX_STOMACH:
                return "RES";
            case CATEGORY_STOOL_INCLUSIONS:
                return "STI";
            case CATEGORY_STOOL_PLENTY:
                return "STP";
            case CATEGORY_ULCERATIVE_COLITIS:
                return "ULC";
            default:
                return "";
        }
    }

    /**
     * @return actual name of the category in the dataset (foldername)
     */
    public String getName(){
        return this.name().replaceAll("CATEGORY_","").replaceAll("_","-").toLowerCase();
    }

    /**
     *
     * @return categorynames as List<String>
     */
    public static List<String> getCategoryNames(){
        List<String> list = new ArrayList<>();
        for(Category c : Category.values()){
            list.add(c.getName());
        }
        return list;
    }
}
