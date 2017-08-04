package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlFile;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import utils.*;
import view.FindViewByIdDialog;

import java.util.ArrayList;
import java.util.List;

public class FindViewByIdAction extends AnAction {
    private FindViewByIdDialog mFindViewByIdDialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Logger.init("FindViewById", Logger.DEBUG);

        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        PsiClass psiClass = PlatformUtils.getPsiClassInEditor(editor);
        if (psiClass == null) {
            Logger.error("当前文件不是一个类文件！");
            return;
        }

        SelectionModel model = editor.getSelectionModel();
        String layoutFileName = model.getSelectedText();
        if (TextUtils.isEmpty(layoutFileName)) {
            layoutFileName = Messages.showInputDialog(project, "layout 文件名：（不需要输入 R.layout. 及文件后缀 .xml）",
                    "未选中 layout 文件名，请输入layout 文件名", Messages.getInformationIcon());
            if (TextUtils.isBlank(layoutFileName)) {
                UIUtils.showPopupBalloon(editor, "未输入 layout 文件名", 5);
                return;
            }
        }

        layoutFileName = StringUtils.removeBlanksInString(layoutFileName);

        XmlFile layoutXmlFile = AndroidUtils.getXmlFileByName(project, layoutFileName);
        if (layoutXmlFile == null) {
            UIUtils.showPopupBalloon(editor, "未找到选中的布局文件", 5);
            return;
        }

        final List<ViewWidgetElement> viewWidgetElements = new ArrayList<>();
        AndroidUtils.getAllViewIdsFromLayoutFile(layoutXmlFile, viewWidgetElements);
        if (!viewWidgetElements.isEmpty()) {
           if (mFindViewByIdDialog != null && mFindViewByIdDialog.isShowing()) {
                mFindViewByIdDialog.cancelDialog();
            }
            mFindViewByIdDialog = new FindViewByIdDialog(editor, psiClass, viewWidgetElements);
            mFindViewByIdDialog.showDialog();
        } else {
            UIUtils.showPopupBalloon(editor, "未找到任何带 id 的 Android View.", 5);
        }
    }
}
