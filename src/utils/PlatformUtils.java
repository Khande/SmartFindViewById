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

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 */
public final class PlatformUtils {

    public static final String METHOD_PARAMS_DELIMITER = ",";

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


    /**
     * 根据当前文件获取其对应的class
     *
     * @param editor 当前项目编辑器
     * @return 当前编辑器内的文件的类
     */
    @Nullable
    public static PsiClass getPsiClassInEditor(@NotNull final Editor editor) {
        PsiElement element = PsiUtilBase.getElementAtCaret(editor);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }


    public static boolean checkStatementExist(@NotNull String srcStatement, @NotNull PsiStatement[] psiStatements) {
        boolean isExist = false;
        for (PsiStatement psiStatement : psiStatements) {
            if (psiStatement.getText().contains(srcStatement)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }


    public static boolean isClassImplementInterface(@NotNull final PsiClass psiClass, @NotNull final String interfaceName) {
        PsiReferenceList implementsList = psiClass.getImplementsList();
        if (implementsList != null) {
            PsiJavaCodeReferenceElement[] referenceElements = implementsList.getReferenceElements();
            for (PsiJavaCodeReferenceElement referenceElement : referenceElements) {
                if (referenceElement.getText().contains(interfaceName)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean isMethodInvoked(@NotNull final PsiStatement statement, @NotNull final String methodName, String... params) {
        String statementText = statement.getText();
        boolean isInvoked = statementText.contains(methodName);
        if (!isInvoked) {
            return false;
        }

        for (int i = 0; i < params.length; i++) {
            isInvoked = isInvoked && statementText.contains(params[i]);
            if (!isInvoked) {
                return false;
            }
        }
        return true;
    }


    @Nullable
    public static String[] extractParamsFromMethodCall(@NotNull PsiMethodCallExpression methodCallExpression) {
        String methodCallExpressionText = StringUtils.removeBlanksInString(methodCallExpression.getText());
        List<String> stringList = StringUtils.extractStringInParentheses(methodCallExpressionText);
        if (stringList.isEmpty()) {
            return null;
        }

        return stringList.get(0).split(METHOD_PARAMS_DELIMITER);
    }


    @Nullable
    public static PsiCodeBlock getSpecifiedMethodBody(@NotNull final PsiClass psiClass, @NotNull final String methodName) {
        PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);
        if (methods.length < 1) {
            return null;
        }

        return methods[0].getBody();
    }


    /**
     * 获取当前光标所在行的字符串（包含前导和末尾的所有字符）
     * @param editor 当前编辑器
     * @return 当前光标所在行的文本
     */
    @NotNull
    static String getCaretLineText(@NotNull Editor editor) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int caretModelOffset = caretModel.getOffset();

        int lineNumber = document.getLineNumber(caretModelOffset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);

        return document.getText(new TextRange(lineStartOffset, lineEndOffset));
    }
}
