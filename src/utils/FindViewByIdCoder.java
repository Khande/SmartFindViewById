package utils;

import com.intellij.openapi.command.WriteCommandAction.Simple;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import entity.ViewWidgetElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Khande on 17/8/5.
 * email: komisha@163.com
 */

public class FindViewByIdCoder extends Simple {

    private static final String TAG = "FindViewByIdCoder";
    public static final String METHOD_NAME_INIT_VIEW = "initView";

    private Editor mEditor;
    private PsiClass mClass;
    private PsiElementFactory mElementFactory;

    private List<ViewWidgetElement> mViewWidgetElements;
    private boolean mIsRootViewFind;
    private String mRootViewName;

    private final List<ViewWidgetElement> mGenerateOnClickElementList = new ArrayList<>();

    public FindViewByIdCoder(@NotNull final Editor editor, @NotNull final PsiClass psiClass,
                             @NotNull final List<ViewWidgetElement> viewWidgetElements,
                             final boolean isRootViewFind, @NotNull final String rootViewName) {
        super(psiClass.getProject(), TAG);
        mEditor = editor;
        mClass = psiClass;

        mViewWidgetElements = viewWidgetElements;
        // 获取Factory
        Project project = psiClass.getProject();
        mElementFactory = JavaPsiFacade.getElementFactory(project);

        mIsRootViewFind = isRootViewFind;
        mRootViewName = rootViewName;

        mGenerateOnClickElementList.clear();
        for (ViewWidgetElement element : mViewWidgetElements) {
            if (element.isGenerateOnClickMethod()) {
                mGenerateOnClickElementList.add(element);
            }
        }
    }

    @Override
    protected void run() throws Throwable {
        try {
            writeViewFieldsCode();
            writeInitViewCode();
            writeOnClickListenerCode();
            writeInitViewStatementIfNeeded();
        } catch (Exception e) {
            UIUtils.showPopupBalloon(mEditor, e.getMessage(), 5);
            Logger.error(e.getMessage());
            return;
        }
        PlatformUtils.prettifyJavaCode(mClass);
        UIUtils.showPopupBalloon(mEditor, StringResourceBundle.getStringByKey("msg_generate_code_success"), 5);
    }


    private void writeViewFieldsCode() {
        PsiField[] existedFields = mClass.getFields();
        for (ViewWidgetElement element : mViewWidgetElements) {
            if (!element.isNeedGenerate()) {
                continue;
            }

            boolean isFieldExist = false;
            for (PsiField existedField : existedFields) {
                String existedFieldName = existedField.getName();
                if (existedFieldName != null && existedFieldName.equals(element.getFieldName())) {
                    isFieldExist = true;
                    break;
                }
            }

            if (!isFieldExist) {
                StringBuilder fieldTextBuilder = new StringBuilder();
                fieldTextBuilder.append("private ");
                fieldTextBuilder.append(element.getViewName());
                fieldTextBuilder.append(" ");
                fieldTextBuilder.append(element.getFieldName());
                fieldTextBuilder.append(";");
                // 添加到class
                mClass.add(mElementFactory.createFieldFromText(fieldTextBuilder.toString(), mClass));
            }
        }
    }


    private void writeInitViewCode() {
        PsiMethod[] initViewMethods = mClass.findMethodsByName(METHOD_NAME_INIT_VIEW, false);
        if (initViewMethods.length > 0 && initViewMethods[0].getBody() != null) {
            PsiCodeBlock initViewMethodBody = initViewMethods[0].getBody();
            PsiStatement[] statements = initViewMethodBody.getStatements();
            for (ViewWidgetElement element : mViewWidgetElements) {
                if (!element.isNeedGenerate()) {
                    continue;
                }

                String findViewByIdCode = "findViewById(" + element.getFullViewId() + ");";
                boolean isFindViewByIdCodeExist = PlatformUtils.checkStatementExist(findViewByIdCode, statements);

                if (!isFindViewByIdCodeExist) {
                    String findViewByIdCodeStatement = buildFindViewByIdStatement(element);
                    initViewMethodBody.add(mElementFactory.createStatementFromText(findViewByIdCodeStatement, initViewMethods[0]));
                }

                if (element.isGenerateOnClickMethod()) {
                    String setOnClickListenerStatement = element.getFieldName() + ".setOnClickListener(this);";
                    boolean isSetOnClickListenerStatementExist = PlatformUtils.checkStatementExist(setOnClickListenerStatement, statements);

                    if (!isSetOnClickListenerStatementExist) {
                        initViewMethodBody.add(mElementFactory.createStatementFromText(setOnClickListenerStatement, initViewMethods[0]));
                    }
                }
            }
        } else {
            StringBuilder initMethodStringBuilder = new StringBuilder("private void initView(");
            initMethodStringBuilder.append(mIsRootViewFind ? "@NonNull final View " + mRootViewName : "");
            initMethodStringBuilder.append(") {\n");
            for (ViewWidgetElement element : mViewWidgetElements) {
                if (!element.isNeedGenerate()) {
                    continue;
                }

                String findViewByIdCodeStatement = buildFindViewByIdStatement(element);
                initMethodStringBuilder.append(findViewByIdCodeStatement);
                if (element.isGenerateOnClickMethod()) {
                    String setOnClickListenerStatement = element.getFieldName() + ".setOnClickListener(this);";
                    initMethodStringBuilder.append(setOnClickListenerStatement);
                }
            }

            initMethodStringBuilder.append("\n}\n");
            mClass.add(mElementFactory.createMethodFromText(initMethodStringBuilder.toString(), mClass));
        }
    }

    @NotNull
    private String buildFindViewByIdStatement(@NotNull final ViewWidgetElement element) {
        StringBuilder findViewByIdCodeBuilder = new StringBuilder();
        findViewByIdCodeBuilder.append(element.getFieldName());
        findViewByIdCodeBuilder.append(" = (");
        findViewByIdCodeBuilder.append(element.getViewName());
        findViewByIdCodeBuilder.append(") ");
        findViewByIdCodeBuilder.append(mIsRootViewFind ? mRootViewName + "." : "");
        findViewByIdCodeBuilder.append("findViewById(");
        findViewByIdCodeBuilder.append(element.getFullViewId());
        findViewByIdCodeBuilder.append(");");
        return findViewByIdCodeBuilder.toString();
    }


    private void writeOnClickListenerCode() {
        if (mGenerateOnClickElementList.isEmpty()) {
            return;
        }

        // 添加未实现的 View.OnClickListener 接口
        boolean isImplementOnClickListener = PlatformUtils.isClassImplementInterface(mClass, AndroidUtils.INTERFACE_NAME_VIEW_ON_CLICK_LISTENER);
        if (!isImplementOnClickListener) {
            PsiReferenceList implementsList = mClass.getImplementsList();

            if (implementsList != null) {
                PsiJavaCodeReferenceElement referenceElementByFQClassName
                        = mElementFactory.createReferenceElementByFQClassName(AndroidUtils.VIEW_ON_CLICK_LISTENER_FQ_CLASS_PATH, mClass.getResolveScope());
                implementsList.add(referenceElementByFQClassName);
            }
        }

        PsiMethod[] onClickMethods = mClass.findMethodsByName(AndroidUtils.METHOD_NAME_ON_CLICK, false);
        if (onClickMethods.length > 0 && onClickMethods[0].getBody() != null) {
            PsiCodeBlock onClickMethodBody = onClickMethods[0].getBody();
            PsiElement[] psiElements = onClickMethodBody.getChildren();
            for (PsiElement psiElement : psiElements) {
                if (psiElement instanceof PsiSwitchStatement) {
                    PsiSwitchStatement psiSwitchStatement = (PsiSwitchStatement) psiElement;
                    PsiCodeBlock psiSwitchStatementBody = psiSwitchStatement.getBody();
                    if (psiSwitchStatementBody != null) {
                        PsiStatement[] psiSwitchStatements = psiSwitchStatementBody.getStatements();
                        for (ViewWidgetElement element : mGenerateOnClickElementList) {
                            boolean isCaseExist = false;
                            for (PsiStatement statement : psiSwitchStatements) {
                                if (statement.getText().contains(element.getFullViewId())) {
                                    isCaseExist = true;
                                    break;
                                }
                            }
                            if (!isCaseExist) {
                                StringBuilder switchCaseBuilder = new StringBuilder("case ");
                                switchCaseBuilder.append(element.getFullViewId());
                                switchCaseBuilder.append(":\n");
                                switchCaseBuilder.append("\t\t\t\t//TODO \n");
                                switchCaseBuilder.append("break;\n");
                                psiSwitchStatementBody.add(
                                        mElementFactory.createStatementFromText(switchCaseBuilder.toString(), psiSwitchStatementBody));
                            }

                        }
                    }

                }
            }
        } else {
            StringBuilder onClickMethodBuilder = new StringBuilder();
            onClickMethodBuilder.append("@Override\n public void onClick(View v) {\n");
            onClickMethodBuilder.append("switch (v.getId()) {\n");
            for (ViewWidgetElement element : mGenerateOnClickElementList) {
                onClickMethodBuilder.append("case ");
                onClickMethodBuilder.append(element.getFullViewId());
                onClickMethodBuilder.append(":\n");
                onClickMethodBuilder.append("\t\t\t\t//TODO \n");
                onClickMethodBuilder.append("break;\n");
            }
            onClickMethodBuilder.append("}\n");
            onClickMethodBuilder.append("}\n");
            mClass.add(mElementFactory.createMethodFromText(onClickMethodBuilder.toString(), mClass));
        }
    }


    private void writeInitViewStatementIfNeeded() {
        boolean isAnActivityClass = AndroidUtils.isActivityClass(mClass);
        if (!isAnActivityClass) {
            return;
        }

        PsiMethod[] onCreateMethods = mClass.findMethodsByName(AndroidUtils.METHOD_NAME_ON_CREATE, false);
        if (onCreateMethods.length < 1) {
            return;
        }

        PsiMethod onCreateMethod = onCreateMethods[0];
        PsiCodeBlock onCreateMethodBody = onCreateMethod.getBody();
        if (onCreateMethodBody == null) {
            return;
        }

        // 获取setContentView
        PsiStatement setContentViewStatement = null;
        // onCreate是否存在initView方法
        boolean hasInitViewStatement = false;
        for (PsiStatement psiStatement : onCreateMethodBody.getStatements()) {
            // 查找setContentView
            if (psiStatement.getFirstChild() instanceof PsiMethodCallExpression) {
                PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) psiStatement.getFirstChild()).getMethodExpression();
                if (methodExpression.getText().contains(AndroidUtils.METHOD_NAME_SET_CONTENT_VIEW)) {
                    setContentViewStatement = psiStatement;
                } else if (methodExpression.getText().contains(METHOD_NAME_INIT_VIEW)) {
                    hasInitViewStatement = true;
                }
            }
        }

        if (!hasInitViewStatement && setContentViewStatement != null) {
            // 将initView()写到setContentView()后面
            onCreateMethodBody.addAfter(mElementFactory.createStatementFromText(METHOD_NAME_INIT_VIEW + "();\n", mClass), setContentViewStatement);
        }
    }

}
