package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import utils.AndroidUtils;
import utils.CreateMethodCreator;
import utils.UIUtils;
import utils.Util;
import view.FindViewByIdDialog;

import java.util.ArrayList;
import java.util.List;

public class FindViewByIdAction extends AnAction {
    private FindViewByIdDialog mDialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取project
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        // 获取选中内容
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (null == editor) {
            return;
        }
        SelectionModel model = editor.getSelectionModel();
        String selectedText = model.getSelectedText();
        // 未选中布局文件名，显示dialog
        if (TextUtils.isEmpty(selectedText)) {
            selectedText= Messages.showInputDialog(project, "layout 文件名：（不需要输入 R.layout. 及文件后缀 .xml）",
                    "未选中 layout 文件名，请输入layout 文件名", Messages.getInformationIcon());
            if (TextUtils.isEmpty(selectedText)) {
                UIUtils.showPopupBalloon(editor, "未输入 layout 文件名", 5);
                return;
            }
        }
        // 获取布局文件，通过FilenameIndex.getFilesByName获取
        // GlobalSearchScope.allScope(project)搜索整个项目
        XmlFile xmlFile = AndroidUtils.getXmlFileByName(project, selectedText);
        if (xmlFile == null) {
            UIUtils.showPopupBalloon(editor, "未找到选中的布局文件", 5);
            return;
        }
        List<ViewWidgetElement> viewWidgetElements = new ArrayList<>();
        AndroidUtils.getAllViewIdsFromLayoutFile(xmlFile, viewWidgetElements);
        // 将代码写入文件，不允许在主线程中进行实时的文件写入
        if (viewWidgetElements.size() != 0) {
            // 判断是否有onCreate/onCreateView方法
            PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
            PsiClass psiClass = Util.getTargetClass(editor, psiFile);
            if (Util.isExtendsActivityOrActivityCompat(project, psiClass)) {
                // 判断是否有onCreate方法
                if (psiClass.findMethodsByName("onCreate", false).length == 0) {
                    // 写onCreate方法
                    new CreateMethodCreator(editor, psiFile, psiClass, "Generate Injections",
                            selectedText, "activity").execute();
                    return;
                }
            } else if (Util.isExtendsFragmentOrFragmentV4(project, psiClass)) {
                // 判断是否有onCreateView方法
                if (psiClass.findMethodsByName("onCreateView", false).length == 0) {
                    new CreateMethodCreator(editor, psiFile, psiClass, "Generate Injections",
                            selectedText, "fragment").execute();
                    return;
                }
            }
            // 有的话就创建变量和findViewById
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancelDialog();
            }
            mDialog = new FindViewByIdDialog(editor, psiClass, viewWidgetElements, selectedText);
            mDialog.showDialog();
        } else {
            UIUtils.showPopupBalloon(editor, "未找到任何Id", 5);
        }
    }
}
