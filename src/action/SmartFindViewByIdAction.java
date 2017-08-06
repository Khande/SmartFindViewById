package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
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

/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 */

public class SmartFindViewByIdAction extends AnAction {
    private FindViewByIdDialog mFindViewByIdDialog;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Logger.init(StringResourceBundle.getStringByKey("plugin_name"), Logger.DEBUG);

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
            Logger.error(StringResourceBundle.getStringByKey("caret_not_in_class"));
            return;
        }


        String layoutFileName = AndroidUtils.tryGetLayoutFileNameAutomatically(editor);

        if (layoutFileName.isEmpty()) {
            layoutFileName = Messages.showInputDialog(project, StringResourceBundle.getStringByKey("msg_input_layout_file_name"),
                    StringResourceBundle.getStringByKey("title_input_layout_file_name_dialog"), Messages.getInformationIcon());
            if (TextUtils.isBlank(layoutFileName)) {
                String fileNameInputError = StringResourceBundle.getStringByKey("error_input_layout_file_name");
                Logger.error(fileNameInputError);
                UIUtils.showPopupBalloon(editor, fileNameInputError, 5);
                return;
            }
        }

        layoutFileName = StringUtils.removeBlanksInString(layoutFileName);

        XmlFile layoutXmlFile = AndroidUtils.getXmlFileByName(project, layoutFileName);
        if (layoutXmlFile == null) {
            String errorLayoutFileNotExistFormat = StringResourceBundle.getStringByKey("error_layout_file_not_exist");
            String fileNotExistError = String.format(errorLayoutFileNotExistFormat, layoutFileName);
            Logger.error(fileNotExistError);
            UIUtils.showPopupBalloon(editor, fileNotExistError, 5);
            return;
        }

        final List<ViewWidgetElement> viewWidgetElements = new ArrayList<>();
        AndroidUtils.getAllViewIdsFromLayoutFile(layoutXmlFile, viewWidgetElements);
        if (!viewWidgetElements.isEmpty()) {
            if (mFindViewByIdDialog != null && mFindViewByIdDialog.isShowing()) {
                mFindViewByIdDialog.closeDialog();
            }
            mFindViewByIdDialog = new FindViewByIdDialog(editor, psiClass, viewWidgetElements);
            mFindViewByIdDialog.showDialog();
        } else {
            String androidViewIsNotFoundError = String.format(
                    StringResourceBundle.getStringByKey("error_android_id_not_found"), layoutFileName);
            Logger.error(androidViewIsNotFoundError);
            UIUtils.showPopupBalloon(editor, androidViewIsNotFoundError, 5);
        }
    }
}
