package utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
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

/**
 * Created by Taylor on 17/5/10.
 */
public final class AndroidUtils {

    private static final String TAG_INCLUDE = "include";
    private static final String TAG_LAYOUT = "layout";
    private static final String TAG_ANDROID_ID = "android:id";
    private static final String TAG_CLASS = "class";
    private static final String TAG_ANDROID_CLICKABLE = "android:clickable";
    private static final String ANDROID_XML_TRUE = "true";
    private static final String XML_FILE_SUFFIX = ".xml";

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

}
