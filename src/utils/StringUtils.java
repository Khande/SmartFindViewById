/*
 * Copyright 2017 Khande
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
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

    /**
     * 提取所有圆括号中的字符串, 如果某个圆括号中无任何字符串（包括空白），则不提取
     * @param targetText 目标提取字符串
     * @return  各个圆括号中的字符串组成的列表
     */
    @NotNull
    public static List<String> extractStringInParentheses(@NotNull String targetText) {
        List<String> extractedList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(targetText);
        while (matcher.find()) {
            extractedList.add(matcher.group());
        }
        return extractedList;
    }
}
