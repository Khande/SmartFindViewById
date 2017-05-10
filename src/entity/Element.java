package entity;

import Utils.Util;
import com.intellij.psi.xml.XmlTag;
import org.apache.http.util.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Element {

    // 判断id正则
    private static final Pattern VIEW_ID_PATTERN = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    // id
    private String id;
    // View 的类名如 TextView
    private String viewName;
    // 命名 1 aa_bb_cc; 2 aaBbCc 3 mBbCcAa
    private int fieldNameType = 3;
    private String fieldName;
    private XmlTag xmlTag;
    // 是否生成
    private boolean isEnable = true;
    // 是否有clickable
    private boolean clickEnable = false;
    // 是否Clickable
    private boolean clickable = false;

    /**
     * 构造函数
     *
     * @param viewNameTag View 控件根布局名称，系统常用控件没有包名，只有类名如 "TextView",
     *                  其他 support 包新控件或自定义控件包括包名和类名, 如 "com.example.CustomView"
     * @param id   android:id 属性字符串
     * @param clickable clickable
     * @throws IllegalArgumentException When the arguments are invalid
     */
    public Element(String viewNameTag, String id, boolean clickable, XmlTag xml) {
        // id
        final Matcher matcher = VIEW_ID_PATTERN.matcher(id);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);
        }

        if (this.id == null) {
            throw new IllegalArgumentException("Invalid format of view id");
        }

        String[] packages = viewNameTag.split("\\.");
        if (packages.length > 1) {
            // com.example.CustomView
            this.viewName = packages[packages.length - 1];
        } else {
            this.viewName = viewNameTag;
        }

        this.clickEnable = clickable;

        this.clickable = clickable;

        this.xmlTag = xml;
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

    public int getFieldNameType() {
        return fieldNameType;
    }

    public void setFieldNameType(int fieldNameType) {
        this.fieldNameType = fieldNameType;
    }

    public XmlTag getXmlTag() {
        return xmlTag;
    }

    public void setXmlTag(XmlTag xmlTag) {
        this.xmlTag = xmlTag;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isClickEnable() {
        return clickEnable;
    }

    public void setClickEnable(boolean clickEnable) {
        this.clickEnable = clickEnable;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /**
     * 获取id，R.id.id
     *
     * @return
     */
    public String getFullId() {
        return "R.id." + id;
    }

    /**
     * 获取变量名
     *
     * @return
     */
    public String getFieldName() {
        if (TextUtils.isEmpty(this.fieldName)) {
            String customFieldName = id;
            String[] names = id.split("_");
            if (fieldNameType == 2) {
                // aaBbCc
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append(names[i]);
                    } else {
                        sb.append(Util.firstToUpperCase(names[i]));
                    }
                }
                customFieldName = sb.toString();
            } else if (fieldNameType == 3) {
                // mBbCcAa
                StringBuilder sb = new StringBuilder();
                sb.append("m");
                for (int i = 1; i < names.length; i++) {
                    sb.append(Util.firstToUpperCase(names[i]));
                }
                sb.append(Util.firstToUpperCase(names[0]));
                customFieldName = sb.toString();
            }
            this.fieldName = customFieldName;
        }
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
       this.fieldName = fieldName;
    }
}
