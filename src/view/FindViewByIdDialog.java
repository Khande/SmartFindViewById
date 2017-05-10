package view;

import utils.Util;
import utils.WidgetFieldCreator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.ui.components.JBScrollPane;
import entity.ViewWidgetElement;
import entity.IdBean;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by wangzai on 2016/11/24.
 */
public class FindViewByIdDialog extends JFrame implements ActionListener {
    private static final String DIALOG_TITLE = "FindViewByIdDialog";
    private static final int DIALOG_WIDTH = 820;
    private static final int DIALOG_HEIGHT = 405;
    private static final String CMD_CHECK_ALL = "全选";
    private static final String CMD_CONFIRM = "确定";
    private static final String CMD_CANCEL = "取消";

    private Editor mEditor;
    private String mSelectedText;
    private List<ViewWidgetElement> mViewWidgetElements;
    // 获取当前文件
    private PsiFile mPsiFile;
    // 获取class
    private PsiClass mClass;
    // 判断是否全选
    private int mElementSize;

    // 标签JPanel
    private JPanel mLabelPanel = new JPanel();
    private JLabel mViewNameLabel = new JLabel("View 类型");
    private JLabel mViewIdLabel = new JLabel("View Id");
    private JLabel mOnClickLabel = new JLabel("OnClick");
    private JLabel mViewFieldNameLabel = new JLabel("目标成员变量名");

    // 内容JPanel
    private JPanel mContentJPanel = new JPanel();
    private GridBagLayout mContentLayout = new GridBagLayout();
    private GridBagConstraints mContentConstraints = new GridBagConstraints();
    // 内容JBScrollPane滚动
    private JBScrollPane jScrollPane;

    // 底部JPanel
    // LayoutInflater JPanel
    private JPanel mInflaterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    // 是否选择LayoutInflater
    private JCheckBox mLayoutInflaterCheckBox = new JCheckBox("LayoutInflater.from(context).inflater", false);
    // 手动修改LayoutInflater字段名
    private JTextField mLayoutInflaterField;
    // 是否全选
    private JCheckBox mCheckAll = new JCheckBox(CMD_CHECK_ALL);
    // 确定、取消JPanel
    private JPanel mPanelButtonRight = new JPanel();
    private JButton mButtonConfirm = new JButton(CMD_CONFIRM);
    private JButton mButtonCancel = new JButton(CMD_CANCEL);

    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagLayout mLayout = new GridBagLayout();
    // GridBagConstraints用来控制添加进的组件的显示位置
    private GridBagConstraints mConstraints = new GridBagConstraints();

    public FindViewByIdDialog(Editor editor, Project project, PsiFile psiFile, PsiClass psiClass, List<ViewWidgetElement> viewWidgetElements, String selectedText) {
        mEditor = editor;
        mSelectedText = selectedText;
        mViewWidgetElements = viewWidgetElements;
        mPsiFile = psiFile;
        mClass = psiClass;
        mElementSize = mViewWidgetElements.size();
        initExist();
        initTopPanel();
        initContentPanel();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    /**
     * 判断已存在的变量，设置全选
     * 判断onclick是否写入
     */
    private void initExist() {
        // 判断是否已存在的变量
        boolean isFdExist = false;
        // 判断是否已存在setOnClickListener
        boolean isClickExist = false;
        // 判断是否存在case R.id.id:
        boolean isCaseExist = false;
        PsiField[] fields = mClass.getFields();
        // 获取initView方法的内容
        PsiStatement[] statements = Util.getInitViewBodyStatements(mClass);
        PsiElement[] onClickStatement = Util.getOnClickStatement(mClass);
        for (ViewWidgetElement mElement : mViewWidgetElements) {
            if (statements != null) {
                for (PsiStatement statement : statements) {
                    if (statement.getText().contains(mElement.getFieldName())
                            && statement.getText().contains("findViewById(" + mElement.getFullViewId() + ");")) {
                        isFdExist = true;
                        break;
                    } else {
                        isFdExist = false;
                    }
                }
                String setOnClickListener = mElement.getFieldName() + ".setOnClickListener(this);";
                for (PsiStatement statement : statements) {
                    if (statement.getText().equals(setOnClickListener)) {
                        isClickExist = true;
                        break;
                    } else {
                        isClickExist = false;
                    }
                }
            }
            if (onClickStatement != null) {
                String cass = "case " + mElement.getFullViewId() + ":";
                for (PsiElement psiElement : onClickStatement) {
                    if (psiElement instanceof PsiSwitchStatement) {
                        PsiSwitchStatement psiSwitchStatement = (PsiSwitchStatement) psiElement;
                        // 获取switch的内容
                        PsiCodeBlock psiSwitchStatementBody = psiSwitchStatement.getBody();
                        if (psiSwitchStatementBody != null) {
                            for (PsiStatement statement : psiSwitchStatementBody.getStatements()) {
                                if (statement.getText().replace("\n", "").replace("break;", "").equals(cass)) {
                                    isCaseExist = true;
                                    break;
                                } else {
                                    isCaseExist = false;
                                }
                            }
                        }
                        if (isCaseExist) {
                            break;
                        }
                    }
                }
            }
            for (PsiField field : fields) {
                String name = field.getName();
                if (name != null && name.equals(mElement.getFieldName()) && isFdExist) {
                    // 已存在的变量设置checkbox为false
                    mElement.setEnable(false);
                    mElementSize--;
                    if (mElement.isClickEnable() && (!isClickExist || !isCaseExist)) {
                        mElement.setClickable(true);
                        mElement.setEnable(true);
                        mElementSize++;
                    }
                    break;
                }
            }
        }
        mCheckAll.setSelected(mElementSize == mViewWidgetElements.size());
        mCheckAll.addActionListener(this);
    }

    /**
     * 添加头部
     */
    private void initTopPanel() {
        mLabelPanel.setLayout(new GridLayout(1, 4, 10, 10));
        mLabelPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        mViewNameLabel.setHorizontalAlignment(JLabel.LEFT);
        mViewNameLabel.setBorder(new EmptyBorder(0, 25, 0, 0));
        mViewIdLabel.setHorizontalAlignment(JLabel.LEFT);
        mOnClickLabel.setHorizontalAlignment(JLabel.LEFT);
        mViewFieldNameLabel.setHorizontalAlignment(JLabel.LEFT);
        mLabelPanel.add(mViewNameLabel);
        mLabelPanel.add(mViewIdLabel);
        mLabelPanel.add(mOnClickLabel);
        mLabelPanel.add(mViewFieldNameLabel);
        mLabelPanel.setSize(720, 30);
        // 添加到JFrame
        getContentPane().add(mLabelPanel, 0);
    }

    /**
     * 添加底部
     */
    private void initBottomPanel() {
        // 添加监听
        mButtonConfirm.addActionListener(this);
        mButtonCancel.addActionListener(this);
        // 左边
        String viewField = "m" + Util.getFieldName(mSelectedText) + "view";
        mLayoutInflaterField = new JTextField(viewField, viewField.length());
        // 右边
        mPanelButtonRight.add(mButtonConfirm);
        mPanelButtonRight.add(mButtonCancel);
        // 添加到JPanel
        mInflaterPanel.add(mCheckAll);
        mInflaterPanel.add(mLayoutInflaterCheckBox);
        mInflaterPanel.add(mLayoutInflaterField);
        // 添加到JFrame
        getContentPane().add(mInflaterPanel, 2);
        getContentPane().add(mPanelButtonRight, 3);
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        mContentJPanel.removeAll();
        // 设置内容
        for (int i = 0; i < mViewWidgetElements.size(); i++) {
            ViewWidgetElement mElement = mViewWidgetElements.get(i);
            IdBean itemJPanel = new IdBean(new GridLayout(1, 4, 10, 10),
                    new EmptyBorder(5, 10, 5, 10),
                    new JCheckBox(mElement.getViewName()),
                    new JLabel(mElement.getId()),
                    new JCheckBox(),
                    new JTextField(mElement.getFieldName()),
                    mElement.isEnable(),
                    mElement.isClickable(),
                    mElement.isClickEnable());
            // 监听
            itemJPanel.setEnableActionListener(enableCheckBox -> mElement.setEnable(enableCheckBox.isSelected()));
            itemJPanel.setClickActionListener(clickCheckBox -> mElement.setClickable(clickCheckBox.isSelected()));
            itemJPanel.setFieldFocusListener(fieldJTextField -> mElement.setFieldName(fieldJTextField.getText()));
            mContentJPanel.add(itemJPanel);
            mContentConstraints.fill = GridBagConstraints.HORIZONTAL;
            mContentConstraints.gridwidth = 0;
            mContentConstraints.gridx = 0;
            mContentConstraints.gridy = i;
            mContentConstraints.weightx = 1;
            mContentLayout.setConstraints(itemJPanel, mContentConstraints);
        }
        mContentJPanel.setLayout(mContentLayout);
        jScrollPane = new JBScrollPane(mContentJPanel);
        jScrollPane.revalidate();
        // 添加到JFrame
        getContentPane().add(jScrollPane, 1);
    }

    /**
     * 设置Constraints
     */
    private void setConstraints() {
        // 使组件完全填满其显示区域
        mConstraints.fill = GridBagConstraints.BOTH;
        // 设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        mConstraints.gridwidth = 0;
        // 第几列
        mConstraints.gridx = 0;
        // 第几行
        mConstraints.gridy = 0;
        // 行拉伸0不拉伸，1完全拉伸
        mConstraints.weightx = 1;
        // 列拉伸0不拉伸，1完全拉伸
        mConstraints.weighty = 0;
        // 设置组件
        mLayout.setConstraints(mLabelPanel, mConstraints);
        mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 1;
        mConstraints.weightx = 1;
        mConstraints.weighty = 1;
        mLayout.setConstraints(jScrollPane, mConstraints);
        mConstraints.fill = GridBagConstraints.HORIZONTAL;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 2;
        mConstraints.weightx = 1;
        mConstraints.weighty = 0;
        mLayout.setConstraints(mInflaterPanel, mConstraints);
        mConstraints.fill = GridBagConstraints.NONE;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 3;
        mConstraints.weightx = 0;
        mConstraints.weighty = 0;
        mConstraints.anchor = GridBagConstraints.EAST;
        mLayout.setConstraints(mPanelButtonRight, mConstraints);
    }

    /**
     * 显示dialog
     */
    public void showDialog() {
        // 显示
        setVisible(true);
    }

    /**
     * 设置JFrame参数
     */
    private void setDialog() {
        // 设置标题
        setTitle(DIALOG_TITLE);
        // 设置布局管理
        setLayout(mLayout);
        // 设置是否可拉伸
        setResizable(true);
        // 设置大小
//        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        // 自适应大小
         pack();
        // 设置居中，放在setSize后面
        setLocationRelativeTo(null);
        // 显示最前
        setAlwaysOnTop(true);
    }

    /**
     * 关闭dialog
     */
    public void cancelDialog() {
        setVisible(false);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case CMD_CONFIRM:
                cancelDialog();
                setCreator(mLayoutInflaterCheckBox.isSelected(), mLayoutInflaterField.getText());
                break;
            case CMD_CANCEL:
                cancelDialog();
                break;
            case CMD_CHECK_ALL:
                // 刷新
                for (ViewWidgetElement mElement : mViewWidgetElements) {
                    mElement.setEnable(mCheckAll.isSelected());
                }
                remove(jScrollPane);
                initContentPanel();
                setConstraints();
                revalidate();
                break;
            default:
                break;
        }
    }

    /**
     * 生成
     *
     * @param isLayoutInflater 是否是LayoutInflater.from(this).inflate(R.layout.activity_main, null);
     * @param text             自定义text
     */
    private void setCreator(boolean isLayoutInflater, String text) {
        new WidgetFieldCreator(this, mEditor, mPsiFile, mClass,
                "Generate Injections", mViewWidgetElements, mSelectedText, isLayoutInflater, text)
                .execute();
    }
}
