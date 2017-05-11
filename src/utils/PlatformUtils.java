package utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Taylor on 17/5/11.
 */
public final class PlatformUtils {
    private PlatformUtils() {
    }


    public static void prettifyJavaCode(@NotNull PsiClass targetClass) {
        Project project = targetClass.getProject();
        PsiFile file = targetClass.getContainingFile();
        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);
        codeStyleManager.optimizeImports(file);
        codeStyleManager.shortenClassReferences(targetClass);
        ReformatCodeProcessor processor = new ReformatCodeProcessor(project, file, null, false);
        processor.runWithoutProgress();
    }


    /**
     * 获得指定的类方法名的第一个方法(考虑到可能存在多个同名的重载方法）的所有执行代码语句
     *
     * @param targetClass 方法所在的类
     * @param methodName  方法名
     * @return 方法体内的所有代码, 空方法返回 {@code null}
     */
    @Nullable
    public static PsiStatement[] getMethodStatements(@NotNull PsiClass targetClass, @NotNull String methodName) {
        PsiMethod[] methods = targetClass.findMethodsByName(methodName, false);
        if (methods.length > 0) {
            PsiCodeBlock methodCodeBlock = methods[0].getBody();
            if (methodCodeBlock != null) {
                return methodCodeBlock.getStatements();
            }
        }
        return null;
    }


}
