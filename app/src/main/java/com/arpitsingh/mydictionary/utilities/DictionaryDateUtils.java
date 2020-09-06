package com.arpitsingh.mydictionary.utilities;


/**
 * Created by ARPIT SINGH
 * 17/10/19
 */


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DictionaryDateUtils {

    private static final String WOD_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_DATE = "01/01/1990";

    public static String getTodayInStringFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(WOD_DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }
}
