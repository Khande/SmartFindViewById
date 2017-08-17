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

package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
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

        VirtualFile currentFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (currentFile == null) {
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

        XmlFile layoutXmlFile;
        Module module = ModuleUtil.findModuleForFile(currentFile, project);
        if (module == null) {
            layoutXmlFile = AndroidUtils.getXmlFileByName(project, layoutFileName);
        } else {
            layoutXmlFile = AndroidUtils.getXmlFileByNameInModule(module, layoutFileName);
        }

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
