package com.yunxin.midnighttarotai.utils;

import android.util.Log;

import com.yunxin.midnighttarotai.R;

public class SpreadUtils {
    /**
     * Maps spread types to their corresponding layout resources
     */
    public static int getLayoutResourceId(String spreadType) {
        switch (spreadType.toLowerCase()) {
            case "onecard":
                return R.layout.spread_one_card;
            case "love cross":
                return R.layout.spread_love_cross;
            case "timeline":
                return R.layout.spread_timeline;
//                case "pastlove":
//                    return R.layout.spread_ex_return;
            case "horseshoe":
                return R.layout.spread_horseshoe;
//                case "companion":
//                    return R.layout.spread_pet;
            case "hexagram":
                return R.layout.spread_hexagram;
            case "two options":
                return R.layout.spread_two_options;
            default:
                Log.w("SpreadType ERROR", "Unknown spread type: " + spreadType);
                return R.layout.spread_one_card;
        }
    }
}
