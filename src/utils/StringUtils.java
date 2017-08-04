package utils;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Taylor on 17/5/10.
 */
public final class StringUtils {
    private StringUtils() {

    }

    @NotNull
    public static String removeBlanksInString(@NotNull final String s) {
        return s.replace(" ", "");
    }

    /**
     * 大写单词首字母
     *
     * @param word 要大写的单词
     * @return 首字母大写的单词
     */
    @NotNull
    public static String capitalize(String word) {
        if (word == null || word.trim().length() < 1) {
            return "";
        } else {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }
    }

    /**
     * 将下划线形式的字符串转换成驼峰形式的字符串, 如 aa_bb_cc ---> AaBbCc
     *
     * @param underscoreText 原始下划线字符串
     * @return 转换后的驼峰字符串
     */
    @NotNull
    public static String transformUnderscore2Camel(@NotNull String underscoreText) {
        if (TextUtils.isBlank(underscoreText)) {
            return "";
        } else {
            StringBuilder camelStrBuilder = new StringBuilder();
            String[] strArr = underscoreText.split("_");
            for (String str : strArr) {
                camelStrBuilder.append(capitalize(str));
            }
            return camelStrBuilder.toString();
        }
    }
}
