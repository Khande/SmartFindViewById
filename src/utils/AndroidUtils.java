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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 */
public final class AndroidUtils {

    public static final String INTERFACE_NAME_VIEW_ON_CLICK_LISTENER = "View.OnClickListener";
    public static final String VIEW_ON_CLICK_LISTENER_FQ_CLASS_PATH = "android.view.View.OnClickListener";
    public static final String ANDROID_APP_ACTIVITY_FQ_PATH = "android.app.Activity";
    public static final String ANDROID_APP_FRAGMENT_FQ_PATH = "android.app.Fragment";
    public static final String ANDROID_SUPPORT_V4_APP_FRAGMENT_FQ_PATH = "android.support.v4.app.Fragment";

    public static final String METHOD_NAME_ON_CLICK = "onClick";
    public static final String METHOD_NAME_ON_CREATE = "onCreate";
    public static final String METHOD_NAME_ON_CREATE_VIEW = "onCreateView";
    public static final String METHOD_NAME_SET_CONTENT_VIEW = "setContentView";
    public static final String METHOD_NAME_ON_CREATE_VIEW_HOLDER = "onCreateViewHolder";

    // 判断id正则
    public static final Pattern VIEW_ID_PATTERN = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    public static final String VIEW_ID_SUFFIX = "R.id.";
    public static final String LAYOUT_RES_SUFFIX = "R.layout.";

    private static final String TAG_INCLUDE = "include";
    private static final String TAG_LAYOUT = "layout";
    private static final String TAG_ANDROID_ID = "android:id";
    private static final String TAG_CLASS = "class";
    private static final String TAG_ANDROID_CLICKABLE = "android:clickable";
    private static final String ANDROID_XML_TRUE = "true";
    private static final String XML_FILE_SUFFIX = ".xml";
    private static final String STRINGS_RES_FILE_NAME = "strings.xml";
    private static final String SRC_MAIN_RES_VALUES_DIR = "src\\main\\res\\values";
    public static final String TAG_STRING = "string";
    public static final String TAG_NAME = "name";

    private AndroidUtils() {

    }


    public static void getAllViewIdsFromLayoutFile(@NotNull final PsiFile layoutFile,
                                                   @NotNull final List<ViewWidgetElement> elements) {
        layoutFile.accept(new XmlRecursiveElementVisitor() {
                              @Override
                              public void visitElement(PsiElement element) {
                                  super.visitElement(element);
                                  if (element instanceof XmlTag) {
                                      XmlTag tag = (XmlTag) element;
                                      String name = tag.getName();
                                      if (name.equalsIgnoreCase(TAG_INCLUDE)) {
                                          XmlAttribute includedLayoutAttr = tag.getAttribute(TAG_LAYOUT, null);
                                          XmlFile includedLayoutFile = null;
                                          if (includedLayoutAttr != null && !TextUtils.isBlank(includedLayoutAttr.getValue())) {
                                              Project project = layoutFile.getProject();
                                              String includedLayoutFileName = getLayoutNameFromIncludeTag(includedLayoutAttr.getValue());
                                              if (!TextUtils.isBlank(includedLayoutFileName)) {
                                                  includedLayoutFile = getXmlFileByName(project, includedLayoutFileName);
                                              }
                                          }
                                          if (includedLayoutFile != null) {
                                              getAllViewIdsFromLayoutFile(includedLayoutFile, elements);
                                          }
                                          return;
                                      }

                                      XmlAttribute idAttr = tag.getAttribute(TAG_ANDROID_ID, null);
                                      if (idAttr == null) {
                                          return;
                                      }
                                      String idValue = idAttr.getValue();
                                      if (TextUtils.isBlank(idValue)) {
                                          return;
                                      }

                                      XmlAttribute classAttr = tag.getAttribute(TAG_CLASS, null);
                                      if (classAttr != null) {
                                          name = classAttr.getName();
                                          if (TextUtils.isBlank(name)) {
                                              return;
                                          }
                                      }

                                      XmlAttribute clickableAttr = tag.getAttribute(TAG_ANDROID_CLICKABLE, null);
                                      boolean clickable = false;
                                      if (clickableAttr != null && !TextUtils.isBlank(clickableAttr.getValue())) {
                                          clickable = clickableAttr.getValue().equals(ANDROID_XML_TRUE);
                                      }


                                      ViewWidgetElement e = new ViewWidgetElement(name, idValue, clickable, tag);
                                      elements.add(e);
                                  }
                              }
                          }
        );

    }


    @Nullable
    private static String getLayoutNameFromIncludeTag(@Nullable String includeLayoutTagValue) {
        if (includeLayoutTagValue == null || !includeLayoutTagValue.startsWith("@") || !includeLayoutTagValue.contains("/")) {
            return null;
        }

        String[] parts = includeLayoutTagValue.split("/");
        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }


    @Nullable
    public static PsiFile getFileByName(@NotNull Project project, @NotNull String fileName) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
        if (psiFiles.length > 0) {
            return psiFiles[0];
        } else {
            return null;
        }
    }

    @Nullable
    public static XmlFile getXmlFileByName(@NotNull Project project, @NotNull String fileName) {
        String xmlFileName = fileName + XML_FILE_SUFFIX;
        return (XmlFile) getFileByName(project, xmlFileName);
    }


    /**
     * 获得项目编写的 strings.xml 文件
     *
     * @param project 当前项目
     * @return src/main/res/values 下的 strings.xml 文件对应的 PsiFile 对象
     */
    @Nullable
    public static PsiFile getStringsResourceFile(@NotNull Project project) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, STRINGS_RES_FILE_NAME, GlobalSearchScope.allScope(project));
        if (psiFiles.length < 1) {
            return null;
        }
        for (PsiFile file : psiFiles) {
            PsiDirectory parentDir = file.getParent();
            if (parentDir != null) {
                String parentDirName = parentDir.toString();
                // 需要考虑项目编译的合并的中间 strings.xml 文件, 这里只考虑项目编写的 strings.xml 文件
                if (parentDirName.contains(SRC_MAIN_RES_VALUES_DIR)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * 从 src/main/values/strings.xml 文件中查找指定 name 为 key 的字符串值
     *
     * @param project 当前 Project
     * @param key     string 键值
     * @return 指定 name 为 key 的字符串值
     */
    @Nullable
    public static String getTextFromStringsResourceFile(@NotNull final Project project, @NotNull final String key) {
        PsiFile stringsResourceFile = getStringsResourceFile(project);
        if (stringsResourceFile == null) {
            return null;
        }

        final StringBuilder value = new StringBuilder();
        stringsResourceFile.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (!(element instanceof XmlTag)) {
                    return;
                }

                XmlTag tag = (XmlTag) element;
                String tagName = tag.getName();
                String nameAttrValue = tag.getAttributeValue(TAG_NAME);
                if (tagName.equals(TAG_STRING) && nameAttrValue != null && nameAttrValue.equals(key)) {
                    PsiElement[] children = tag.getChildren();
                    StringBuilder valueBuilder = new StringBuilder();
                    for (PsiElement e : children) {
                        valueBuilder.append(e.getText());
                    }

                    Pattern p = Pattern.compile("<string name=\"" + key + "\">(.*)</string>");
                    Matcher matcher = p.matcher(valueBuilder.toString());
                    while (matcher.find()) {
                        value.delete(0, value.length());
                        value.append(matcher.group(1));
                    }
                }


            }
        });
        return value.toString();
    }


    /**
     * 判断当前打开的类文件是否是一个 Activity class
     * 通过判断是 android.app.Activity 类的直接子类或间接子类来确定，
     * 如继承了 AppCompatActivity, FragmentActivity 都是 Activity 的子类，因为 AppCompatActivity 等是 Activity 的间接子类.
     *
     * @param psiClass current opened & displayed file's class
     * @return <code>true</code> means yes, <code>false</code> means no
     */
    public static boolean isActivityClass(@NotNull final PsiClass psiClass) {
        Project project = psiClass.getProject();
        EverythingGlobalScope globalScope = new EverythingGlobalScope(project);
        PsiClass activityClass = JavaPsiFacade.getInstance(project).findClass(ANDROID_APP_ACTIVITY_FQ_PATH, globalScope);
        return (activityClass != null && psiClass.isInheritor(activityClass, true));
    }


    /**
     * 如果当前类是一个 Activity 类，则在其 onCreate 方法的 setContentView 方法调用语句中获取当前 Activity 的布局资源文件名.
     * 不考虑其他的一些在其他地方定义布局文件资源的类封装
     *
     * @param psiClass 指定的类
     * @return 当前 Activity 的布局资源文件名
     */
    @NotNull
    private static String getLayoutFileNameInActivity(@NotNull PsiClass psiClass) {
        if (!isActivityClass(psiClass)) {
            return "";
        }

        PsiCodeBlock onCreateMethodBody = PlatformUtils.getSpecifiedMethodBody(psiClass, METHOD_NAME_ON_CREATE);
        if (onCreateMethodBody == null) {
            return "";
        }

        String layoutFileName = "";

        for (PsiStatement psiStatement : onCreateMethodBody.getStatements()) {
            if (!layoutFileName.isEmpty()) {
                break;
            }
            // 查找setContentView
            PsiElement psiElement = psiStatement.getFirstChild();
            if (psiElement instanceof PsiMethodCallExpression) {
                PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) psiElement).getMethodExpression();
                if (methodExpression.getText().equals(METHOD_NAME_SET_CONTENT_VIEW)) {
                    String[] methodCallParams = PlatformUtils.extractParamsFromMethodCall((PsiMethodCallExpression) psiElement);
                    if (methodCallParams != null && methodCallParams.length > 0) {
                        String fullLayoutFilePath = methodCallParams[0];
                        layoutFileName = fullLayoutFilePath.replace(LAYOUT_RES_SUFFIX, "");
                        break;
                    }
                }
            }
        }

        return layoutFileName;
    }


    /**
     * 判断指定 class 是不是直接或间接继承 android.app.Fragment 或者 android.support.v4.app.Fragment 类
     *
     * @param psiClass 指定的 class
     * @return <code>true</code> means yes, <code>false</code> means no
     */
    public static boolean isFragmentClass(@NotNull final PsiClass psiClass) {
        Project project = psiClass.getProject();
        EverythingGlobalScope globalScope = new EverythingGlobalScope(project);
        PsiClass fragmentClass = JavaPsiFacade.getInstance(project).findClass(ANDROID_APP_FRAGMENT_FQ_PATH, globalScope);
        PsiClass fragmentV4Class = JavaPsiFacade.getInstance(project).findClass(ANDROID_SUPPORT_V4_APP_FRAGMENT_FQ_PATH, globalScope);
        return (fragmentClass != null && psiClass.isInheritor(fragmentClass, true))
                || (fragmentV4Class != null && psiClass.isInheritor(fragmentV4Class, true));
    }


    private static String getLayoutFileNameInFragment(@NotNull final PsiClass psiClass) {
        if (!isFragmentClass(psiClass)) {
            return "";
        }

        PsiCodeBlock onCreateViewMethodBody = PlatformUtils.getSpecifiedMethodBody(psiClass, METHOD_NAME_ON_CREATE_VIEW);
        if (onCreateViewMethodBody == null) {
            return "";
        }

        PsiStatement[] onCreateViewMethodBodyStatements = onCreateViewMethodBody.getStatements();

        String layoutFileName = "";

        for (PsiStatement statement : onCreateViewMethodBodyStatements) {
            if (!layoutFileName.isEmpty()) {
                break;
            }
            String statementText = statement.getText();
            layoutFileName = findLayoutFileNameInText(statementText);
        }

        return layoutFileName;
    }



    @NotNull
    private static String getLayoutFileNameInRecyclerViewAdapter(@NotNull final PsiClass psiClass) {
        PsiCodeBlock onCreateViewHolderMethodBody = PlatformUtils.getSpecifiedMethodBody(psiClass, METHOD_NAME_ON_CREATE_VIEW_HOLDER);
        if (onCreateViewHolderMethodBody == null) {
            return "";
        }

        String layoutFileName = "";

        PsiStatement[] onCreateViewHolderMethodBodyStatements = onCreateViewHolderMethodBody.getStatements();
        for (PsiStatement statement : onCreateViewHolderMethodBodyStatements) {
            if (!layoutFileName.isEmpty()) {
                break;
            }
            String statementText = statement.getText();
            layoutFileName = findLayoutFileNameInText(statementText);
        }

        return layoutFileName;
    }


    @NotNull
    private static String tryGetLayoutFileNameBySelectedText(@NotNull final Editor editor) {
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (TextUtils.isBlank(selectedText)) {
            return "";
        } else {
            Project project = editor.getProject();
            if (project == null) {
                return "";
            }
            String layoutFileName = StringUtils.removeBlanksInString(selectedText);
            XmlFile xmlFile = getXmlFileByName(project, layoutFileName);
            if (xmlFile != null) {
                return layoutFileName;
            } else {
                return "";
            }
        }
    }


    @NotNull
    private static String tryGetLayoutFileNameInCaretLine(@NotNull final Editor editor) {
        String currentLineText = PlatformUtils.getCaretLineText(editor);
        return findLayoutFileNameInText(currentLineText);
    }


    @NotNull
    private static String findLayoutFileNameInText(@NotNull final String srcText) {
        String srcTextWithoutBlanks = StringUtils.removeBlanksInString(srcText);
        int index = srcTextWithoutBlanks.indexOf(LAYOUT_RES_SUFFIX);
        if (index < 0) {
            return "";
        }

        StringBuilder layoutFileNameBuilder = new StringBuilder("");
        char[] chars = srcTextWithoutBlanks.substring(index + LAYOUT_RES_SUFFIX.length()).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ',' || chars[i] == ')') {
                break;
            }
            layoutFileNameBuilder.append(chars[i]);
        }

        return layoutFileNameBuilder.toString();
    }


    @NotNull
    public static String tryGetLayoutFileNameAutomatically(@NotNull final Editor editor) {
        PsiClass psiClass = PlatformUtils.getPsiClassInEditor(editor);
        if (psiClass == null) {
            return "";
        }

        String layoutFileName = tryGetLayoutFileNameBySelectedText(editor);
        if (!layoutFileName.isEmpty()) {
            return layoutFileName;
        }

        layoutFileName = tryGetLayoutFileNameInCaretLine(editor);
        if (!layoutFileName.isEmpty()) {
            return layoutFileName;
        }

        layoutFileName = getLayoutFileNameInActivity(psiClass);
        if (!layoutFileName.isEmpty()) {
            return layoutFileName;
        }


        layoutFileName = getLayoutFileNameInFragment(psiClass);
        if (!layoutFileName.isEmpty()) {
            return layoutFileName;
        }

        layoutFileName = getLayoutFileNameInRecyclerViewAdapter(psiClass);
        if (!layoutFileName.isEmpty()) {
            return layoutFileName;
        }


        return layoutFileName;
    }




}
