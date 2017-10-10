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

package entity;

import com.intellij.psi.xml.XmlTag;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import utils.AndroidUtils;
import utils.StringUtils;

import java.util.regex.Matcher;

/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 *
 * layout xml 文件中 view Widget 元素
 */
public class ViewWidgetElement {

    // android:id 属性字符串
    private String id;

    // view 的类名如 TextView
    private String viewName;

    // 生成代码所在 Activity 或 Fragment 成员变量名
    private String fieldName;


    private XmlTag xmlTag;

    private boolean isNeedGenerate = true;

    private boolean isGenerateOnClickMethod = false;

    // 是否Clickable
    private boolean clickable = false;

    /**
     * 构造函数
     *
     * @param viewNameTag view 控件根布局名称，系统常用控件没有包名，只有类名如 "TextView",
     *                    其他 support 包新控件或自定义控件包括包名和类名, 如 "com.example.CustomView"
     * @param id          android:id 属性字符串
     * @param clickable   clickable
     * @throws IllegalArgumentException When the arguments are invalid
     */
    public ViewWidgetElement(@NotNull String viewNameTag, String id, boolean clickable, XmlTag xml) {
        final Matcher matcher = AndroidUtils.VIEW_ID_PATTERN.matcher(id);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);
        }

        if (this.id == null) {
            throw new IllegalArgumentException("Invalid format of view id");
        }

        this.viewName = extractViewName(viewNameTag);

        this.clickable = clickable;

        setGenerateOnClickMethod(clickable, viewName);

        this.xmlTag = xml;
    }

    @NotNull
    private String extractViewName(@NotNull String viewNameTag) {
        String[] packages = viewNameTag.split("\\.");
        String viewName;
        if (packages.length > 1) {
            // com.example.CustomView
            viewName = packages[packages.length - 1];
        } else {
            viewName = viewNameTag;
        }
        return viewName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public XmlTag getXmlTag() {
        return xmlTag;
    }

    public void setXmlTag(XmlTag xmlTag) {
        this.xmlTag = xmlTag;
    }

    public boolean isNeedGenerate() {
        return isNeedGenerate;
    }

    public void setNeedGenerate(boolean needGenerate) {
        isNeedGenerate = needGenerate;
    }

    public boolean isGenerateOnClickMethod() {
        return isGenerateOnClickMethod;
    }

    private void setGenerateOnClickMethod(final boolean clickable, @NotNull final String viewName) {
        isGenerateOnClickMethod = clickable || (viewName.contains("Button") && !viewName.contains("Radio"));
    }

    public void setGenerateOnClickMethod(boolean generateOnClickMethod) {
        this.isGenerateOnClickMethod = generateOnClickMethod;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /**
     * 获取 view 的完整 id
     *
     * @return 形如 R.id.aa_bb_cc 的 android:id 字符串
     */
    public String getFullViewId() {
        return AndroidUtils.VIEW_ID_SUFFIX + id;
    }

    /**
     * 生成该 view id 对应的 view 代码所在 Activity 或 Fragment 成员变量名
     * 此处规范为 R.id.aa_bb_cc --> mBbCcAa, 一般 aa 为 view 缩写，如 TextView 缩写为 tv.
     *
     * @return view 所对应的成员变量名字符串
     */
    @NotNull
    public String getFieldName() {
        if (TextUtils.isEmpty(this.fieldName)) {
            String[] names = id.split("_");
            StringBuilder sb = new StringBuilder();
            sb.append("m");
            for (int i = 1; i < names.length; i++) {
                sb.append(StringUtils.capitalize(names[i]));
            }
            sb.append(StringUtils.capitalize(names[0]));
            this.fieldName = sb.toString();
        }
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
