package utils;

import com.intellij.openapi.command.WriteCommandAction.Simple;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WidgetFieldCreator extends Simple {

    private static final String TAG = "WidgetFieldCreator";
    public static final String METHOD_NAME_INIT_VIEW = "initView";

    private Editor mEditor;
    private Project mProject;
    private PsiClass mClass;
    private List<ViewWidgetElement> mViewWidgetElements;
    private PsiElementFactory mFactory;
    private boolean mIsRootViewFind;
    private String mRootViewName;

    private List<ViewWidgetElement> mOnClickList = new ArrayList<>();

    public WidgetFieldCreator(@NotNull final Editor editor, @NotNull final PsiClass psiClass,
                              @NotNull final List<ViewWidgetElement> viewWidgetElements,
                              final boolean isRootViewFind, @NotNull final String rootViewName) {
        super(psiClass.getProject(), TAG);
        mEditor = editor;
        mProject = psiClass.getProject();
        mClass = psiClass;
        mViewWidgetElements = viewWidgetElements;
        // 获取Factory
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        mIsRootViewFind = isRootViewFind;
        mRootViewName = rootViewName;
        for (ViewWidgetElement mElement : mViewWidgetElements) {
            if (mElement.isEnable() && mElement.isClickEnable() && mElement.isClickable()) {
                mOnClickList.add(mElement);
            }
        }
    }

    @Override
    protected void run() throws Throwable {
        try {
            writeViewFieldsCode();
            writeInitViewCode();
            writeOnClickListenerCode();
        } catch (Exception e) {
            UIUtils.showPopupBalloon(mEditor, e.getMessage(), 10);
            return;
        }
        PlatformUtils.prettifyJavaCode(mClass);
        UIUtils.showPopupBalloon(mEditor, "生成成功", 5);
    }


    private void writeViewFieldsCode() {
        PsiField[] existedFields = mClass.getFields();
        for (ViewWidgetElement element : mViewWidgetElements) {
            boolean isFieldExist = false;
            for (PsiField existedField : existedFields) {
                String existedFieldName = existedField.getName();
                if (existedFieldName != null && existedFieldName.equals(element.getFieldName())) {
                    isFieldExist = true;
                    break;
                }
            }

            if (!isFieldExist && element.isEnable()) {
                StringBuilder fieldTextBuilder = new StringBuilder();
                fieldTextBuilder.append("private ");
                fieldTextBuilder.append(element.getViewName());
                fieldTextBuilder.append(" ");
                fieldTextBuilder.append(element.getFieldName());
                fieldTextBuilder.append(";");
                // 添加到class
                mClass.add(mFactory.createFieldFromText(fieldTextBuilder.toString(), mClass));
            }
        }
    }


    private void writeInitViewCode() {
        PsiMethod[] initViewMethods = mClass.findMethodsByName(METHOD_NAME_INIT_VIEW, false);
        if (initViewMethods.length > 0 & initViewMethods[0].getBody() != null) {
            PsiCodeBlock initViewMethodBody = initViewMethods[0].getBody();
            PsiStatement[] statements = initViewMethodBody.getStatements();
            for (ViewWidgetElement element : mViewWidgetElements) {
                if (element.isEnable()) {
                    String findViewByIdCode = "findViewById(" + element.getFullViewId() + ");";
                    boolean isFindViewByIdCodeExist = PlatformUtils.checkStatementExist(findViewByIdCode, statements);

                    if (!isFindViewByIdCodeExist) {
                        String findViewByIdCodeStatement = buildFindViewByIdStatement(element);
                        initViewMethodBody.add(mFactory.createStatementFromText(findViewByIdCodeStatement, initViewMethods[0]));
                    }

                    if (element.isClickEnable() && element.isClickable()) {
                        String setOnClickListenerStatement = element.getFieldName() + ".setOnClickListener(this);";
                        boolean isSetOnClickListenerStatementExist = PlatformUtils.checkStatementExist(setOnClickListenerStatement, statements);

                        if (!isSetOnClickListenerStatementExist) {
                            initViewMethodBody.add(mFactory.createStatementFromText(setOnClickListenerStatement, initViewMethods[0]));
                        }
                    }
                }
            }
        } else {
            StringBuilder initMethodStringBuilder = new StringBuilder("private void initView(");
            initMethodStringBuilder.append(mIsRootViewFind ? "@NonNull final View " + mRootViewName : "");
            initMethodStringBuilder.append(") {\n");
            for (ViewWidgetElement element : mViewWidgetElements) {
                if (element.isEnable()) {
                    String findViewByIdCodeStatement = buildFindViewByIdStatement(element);
                    initMethodStringBuilder.append(findViewByIdCodeStatement);
                }

                if (element.isClickable() && element.isClickEnable()) {
                    String setOnClickListenerStatement = element.getFieldName() + ".setOnClickListener(this);";
                    initMethodStringBuilder.append(setOnClickListenerStatement);
                }

            }

            initMethodStringBuilder.append("\n}\n");
            mClass.add(mFactory.createMethodFromText(initMethodStringBuilder.toString(), mClass));
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
        if (mOnClickList.isEmpty()) {
            return;
        }

        // 添加未实现的 View.OnClickListener 接口
        boolean isImplementOnClickListener = PlatformUtils.isClassImplementInterface(mClass, "View.OnClickListener");
        if (!isImplementOnClickListener) {
            PsiReferenceList implementsList = mClass.getImplementsList();

            if (implementsList != null) {
                PsiJavaCodeReferenceElement referenceElementByFQClassName
                        = mFactory.createReferenceElementByFQClassName("android.view.View.OnClickListener", mClass.getResolveScope());
                implementsList.add(referenceElementByFQClassName);
            }
        }

        PsiMethod[] onClickMethods = mClass.findMethodsByName("onClick", false);
        if (onClickMethods.length > 0 && onClickMethods[0].getBody() != null) {

        }








    }

    /**
     * 添加实现OnClickListener接口
     */
    private void generateOnClickCode() {

       // 判断是否已有onClick方法
        PsiMethod[] onClickMethods = mClass.findMethodsByName("onClick", false);
        // 已有onClick方法
        if (onClickMethods.length > 0 && onClickMethods[0].getBody() != null) {
            PsiCodeBlock onClickMethodBody = onClickMethods[0].getBody();
            // 获取switch
            for (PsiElement psiElement : onClickMethodBody.getChildren()) {
                if (psiElement instanceof PsiSwitchStatement) {
                    PsiSwitchStatement psiSwitchStatement = (PsiSwitchStatement) psiElement;
                    // 获取switch的内容
                    PsiCodeBlock psiSwitchStatementBody = psiSwitchStatement.getBody();
                    if(psiSwitchStatementBody != null) {
                        for (ViewWidgetElement element : mOnClickList) {
                            String cass = "case " + element.getFullViewId() + ":";
                            // 判断是否存在
                            boolean isExist = false;
                            for (PsiStatement statement : psiSwitchStatementBody.getStatements()) {
                                if (statement.getText().replace("\n","").replace("break;","").equals(cass)) {
                                    isExist = true;
                                    break;
                                } else {
                                    isExist = false;
                                }
                            }
                            // 不存在就添加
                            if (!isExist) {
                                psiSwitchStatementBody.add(mFactory.createStatementFromText(cass, psiSwitchStatementBody));
                                psiSwitchStatementBody.add(mFactory.createStatementFromText("break;", psiSwitchStatementBody));
                            }
                        }
                    }
                }
            }
        } else {
            if (mOnClickList.size() != 0) {
                StringBuilder onClick = new StringBuilder();
                onClick.append("@Override public void onClick(view v) {\n");
                onClick.append("switch (v.getId()) {\n");
                for (ViewWidgetElement mElement : mOnClickList) {
                    if (mElement.isClickable()) {
                        onClick.append("case " + mElement.getFullViewId() + ":\nbreak;\n");
                    }
                }
                onClick.append("}\n");
                onClick.append("}\n");
                mClass.add(mFactory.createMethodFromText(onClick.toString(), mClass));
            }
        }
    }

    /**
     * 设置变量的值FindViewById，Activity和Fragment
     */
    private void generateFindViewById() {
        if (AndroidUtils.isAnActivityClass(mClass)) {
            // 判断是否有onCreate方法
            if (mClass.findMethodsByName("onCreate", false).length == 0) {
            } else {
                writeViewFieldsCode();

                // 获取setContentView
                PsiStatement setContentViewStatement = null;
                // onCreate是否存在initView方法
                boolean hasInitViewStatement = false;

                PsiMethod onCreate = mClass.findMethodsByName("onCreate", false)[0];
                for (PsiStatement psiStatement : onCreate.getBody().getStatements()) {
                    // 查找setContentView
                    if (psiStatement.getFirstChild() instanceof PsiMethodCallExpression) {
                        PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) psiStatement.getFirstChild()).getMethodExpression();
                        if (methodExpression.getText().equals("setContentView")) {
                            setContentViewStatement = psiStatement;
                        } else if (methodExpression.getText().equals("initView")) {
                            hasInitViewStatement = true;
                        }
                    }
                }

                if (!hasInitViewStatement && setContentViewStatement != null) {
                    // 将initView()写到setContentView()后面
                    onCreate.getBody().addAfter(mFactory.createStatementFromText("initView();", mClass), setContentViewStatement);
                }

                generatorLayoutCode(null, "getApplicationContext()");
            }

        } else if (Util.isExtendsFragmentOrFragmentV4(mProject, mClass)) {
            // 判断是否有onCreateView方法
            if (mClass.findMethodsByName("onCreateView", false).length == 0) {

            } else {
                writeViewFieldsCode();
                // 查找onCreateView
                PsiReturnStatement returnStatement = null;
                // view
                String returnValue = null;
                // onCreateView是否存在initView方法
                boolean hasInitViewStatement = false;

                PsiMethod onCreate = mClass.findMethodsByName("onCreateView", false)[0];
                for (PsiStatement psiStatement : onCreate.getBody().getStatements()) {
                    if (psiStatement instanceof PsiReturnStatement) {
                        // 获取view的值
                        returnStatement = (PsiReturnStatement) psiStatement;
                        returnValue = returnStatement.getReturnValue().getText();
                    } else if (psiStatement.getFirstChild() instanceof PsiMethodCallExpression) {
                        PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) psiStatement.getFirstChild()).getMethodExpression();
                        if (methodExpression.getText().equals("initView")) {
                            hasInitViewStatement = true;
                        }
                    }
                }

                if (!hasInitViewStatement && returnStatement != null && returnValue != null) {
                    onCreate.getBody().addBefore(mFactory.createStatementFromText("initView(" + returnValue + ");", mClass), returnStatement);
                }
                generatorLayoutCode(returnValue, "getActivity()");
            }
        }
    }

    /**
     * 写initView方法
     *
     * @param findPre Fragment的话要view.findViewById
     * @param context
     */
    private void generatorLayoutCode(String findPre, String context) {
    }

}
