package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;

import java.awt.*;

/**
 * Created by Taylor on 17/5/11.
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
