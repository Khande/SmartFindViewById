package utils;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Khande on 17/8/6.
 */
public final class StringResourceBundle {

    private static final String STRING_RESOURCE_BUNDLE_BASE_NAME = "StringsBundle";

//    private final static ResourceBundle STRING_RESOURCE_BUNDLE = ResourceBundle.getBundle(STRING_RESOURCE_BUNDLE_BASE_NAME, Locale.getDefault());
    private final static ResourceBundle STRING_RESOURCE_BUNDLE = ResourceBundle.getBundle(STRING_RESOURCE_BUNDLE_BASE_NAME, new Locale("en", "US"));

    private StringResourceBundle() {
        //no instance
    }


    public static String getStringByKey(@NotNull final String key) {
        String value = "";

        try {
            value = new String(STRING_RESOURCE_BUNDLE.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return value;
    }


}
