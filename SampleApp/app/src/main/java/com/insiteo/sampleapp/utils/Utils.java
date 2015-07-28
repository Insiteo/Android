package com.insiteo.sampleapp.utils;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by MMO on 09/07/2015.
 */
public class Utils {

    public static void vibrate(Context context, long duration) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(duration);
    }

}
