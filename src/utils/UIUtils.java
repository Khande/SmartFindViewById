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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;

import java.awt.*;

/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 */
public final class UIUtils {
    private UIUtils() {

    }


    /**
     * 显示提示信息气泡
     *
     * @param editor   Editor
     * @param message  显示消息内容
     * @param duration 显示时间，单位秒
     */
    public static void showPopupBalloon(final Editor editor, final String message, final int duration) {
        ApplicationManager.getApplication().invokeLater(() -> {
            JBPopupFactory factory = JBPopupFactory.getInstance();
            factory.createHtmlTextBalloonBuilder(message, null,
                    new JBColor(new Color(116, 214, 238), new Color(76, 112, 117)),
                    null)
                    .setFadeoutTime(duration * 1000)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
        });
    }
}
