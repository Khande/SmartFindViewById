package utils;

/**
 * Created by Taylor on 17/5/10.
 */
public final class StringUtils {
    private StringUtils() {

    }

    /**
     * 大写单词首字母
     * @param word 要大写的单词
     * @return 首字母大写的单词
     */
    public static String capitalize(String word) {
        if (word == null || word.trim().length() < 1) {
            return "";
        } else {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }
    }
}
