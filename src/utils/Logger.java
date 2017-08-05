package utils;

import com.intellij.notification.*;


/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 */
public class Logger {
    private static String sName;
    private static int sLevel = 0;

    public static final int DEBUG = 3;
    public static final int INFO = 2;
    public static final int WARN = 1;
    public static final int ERROR = 0;

    public static void init(String name,int level) {
        sName = name;
        sLevel = level;
        NotificationsConfiguration.getNotificationsConfiguration().register(sName, NotificationDisplayType.NONE);
    }

    public static void debug(String text) {
        if (sLevel >= DEBUG) {
            Notifications.Bus.notify(
                    new Notification(sName, sName + " [DEBUG]", text, NotificationType.INFORMATION));
        }
    }

    public static void info(String text) {
        if (sLevel > INFO) {
            Notifications.Bus.notify(
                    new Notification(sName, sName + " [INFO]", text, NotificationType.INFORMATION));
        }
    }

    public static void warn(String text) {
        if (sLevel > WARN) {
            Notifications.Bus.notify(
                    new Notification(sName, sName + " [WARN]", text, NotificationType.WARNING));
        }
    }

    public static void error(String text) {
        if (sLevel > ERROR) {
            Notifications.Bus.notify(
                    new Notification(sName, sName + " [ERROR]", text, NotificationType.ERROR));
        }
    }
}
