package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;

import java.awt.*;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    /**
     * 根据当前文件获取对应的class文件
     *
     * @param editor
     * @param file
     * @return
     */
    public static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }

    /**
     * 判断mClass是不是继承activityClass或者activityCompatClass
     *
     * @param mProject
     * @param mClass
     * @return
     */
    public static boolean isExtendsActivityOrActivityCompat(Project mProject, PsiClass mClass) {
        // 根据类名查找类
        PsiClass activityClass = JavaPsiFacade.getInstance(mProject).findClass("android.app.Activity", new EverythingGlobalScope(mProject));
        PsiClass activityCompatClass = JavaPsiFacade.getInstance(mProject).findClass("android.support.v7.app.AppCompatActivity", new EverythingGlobalScope(mProject));
        return (activityClass != null && mClass.isInheritor(activityClass, true))
                || (activityCompatClass != null && mClass.isInheritor(activityCompatClass, true))
                || mClass.getName().contains("Activity");
    }

    /**
     * 判断mClass是不是继承fragmentClass或者fragmentV4Class
     *
     * @param mProject
     * @param mClass
     * @return
     */
    public static boolean isExtendsFragmentOrFragmentV4(Project mProject, PsiClass mClass) {
        // 根据类名查找类
        PsiClass fragmentClass = JavaPsiFacade.getInstance(mProject).findClass("android.app.Fragment", new EverythingGlobalScope(mProject));
        PsiClass fragmentV4Class = JavaPsiFacade.getInstance(mProject).findClass("android.support.v4.app.Fragment", new EverythingGlobalScope(mProject));
        return (fragmentClass != null && mClass.isInheritor(fragmentClass, true))
                || (fragmentV4Class != null && mClass.isInheritor(fragmentV4Class, true))
                || mClass.getName().contains("Fragment");
    }

    /**
     * 创建onCreate方法
     *
     * @param mSelectedText
     * @return
     */
    public static String createOnCreateMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override protected void onCreate(android.os.Bundle savedInstanceState) {\n");
        method.append("super.onCreate(savedInstanceState);\n");
        method.append("\t// TODO:OnCreate Method has been created, run FindViewById again to generate code\n");
        method.append("\tsetContentView(R.layout.");
        method.append(mSelectedText);
        method.append(");\n");
        method.append("}");
        return method.toString();
    }

    /**
     * 创建onCreateView方法
     *
     * @param mSelectedText
     * @return
     */
    public static String createOnCreateViewMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override public view onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {\n");
        method.append("\t// TODO:OnCreateView Method has been created, run FindViewById again to generate code\n");
        method.append("\tview view = view.inflate(getActivity(), R.layout.");
        method.append(mSelectedText);
        method.append(", null);");
        method.append("return view;");
        method.append("}");
        return method.toString();
    }

    /**
     * 判断是否实现了OnClickListener接口
     *
     * @param referenceElements
     * @return
     */
    public static boolean isImplementsOnClickListener(PsiJavaCodeReferenceElement[] referenceElements) {
        for (PsiJavaCodeReferenceElement referenceElement : referenceElements) {
            if (referenceElement.getText().contains("OnClickListener")) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取onClick方法里面的每条数据
     *
     * @param mClass
     * @return
     */
    public static PsiElement[] getOnClickStatement(PsiClass mClass) {
        // 获取onClick方法
        PsiMethod[] onClickMethods = mClass.findMethodsByName("onClick", false);
        PsiElement[] psiElements = null;
        if (onClickMethods.length > 0 && onClickMethods[0].getBody() != null) {
            PsiCodeBlock onClickMethodBody = onClickMethods[0].getBody();
            psiElements = onClickMethodBody.getChildren();
        }
        return psiElements;
    }
}
