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
}
