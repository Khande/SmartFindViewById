package view;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.ui.components.JBScrollPane;
import entity.IdBean;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.PlatformUtils;
import utils.Util;
import utils.WidgetFieldCreator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static utils.WidgetFieldCreator.METHOD_NAME_INIT_VIEW;

/**
 * Created by khande on 2017/05/11.
 */
public class FindViewByIdDialog extends JDialog implements ActionListener {
    private static final String DIALOG_TITLE = "FindViewByIdDialog";
    private static final String CMD_CHECK_ALL_VIEW_WIDGETS = "全选";
    private static final String CMD_CHECK_ROOT_VIEW = "RootView";
    private static final String CMD_CONFIRM = "确定";
    private static final String CMD_CANCEL = "取消";
    private static final String ROOT_VIEW_NAME_DEFAULT = "itemView";

    private static final int LEFT_INSET = 28;
    private static final int RIGHT_INSET = 28;
    private static final int IN_GROUP_VERTICAL_GAP = 12;
    private static final int OUT_GROUP_VERTICAL_GAP = 20;

    private Editor mEditor;
    private String mLayoutFileName;
    private List<ViewWidgetElement> mViewWidgetElements;
    // 获取class
    private PsiClass mClass;
    // 判断是否全选
    private int mElementSize;


    private final JButton mConfirmButton = new JButton(CMD_CONFIRM);

    private final JCheckBox mCheckAllViewWidgetsCheckBox = new JCheckBox(CMD_CHECK_ALL_VIEW_WIDGETS);
    private final JCheckBox mRootViewCheckBox = new JCheckBox(CMD_CHECK_ROOT_VIEW);
    private final JTextField mRootViewNameTextField = new JTextField(ROOT_VIEW_NAME_DEFAULT, ROOT_VIEW_NAME_DEFAULT.length() + 4);


    // 内容JPanel
    private JPanel mContentJPanel = new JPanel();
    private GridBagLayout mContentLayout = new GridBagLayout();
    private GridBagConstraints mContentConstraints = new GridBagConstraints();
    // 内容JBScrollPane滚动
    private JBScrollPane jScrollPane;

    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagLayout mLayout = new GridBagLayout();
    // GridBagConstraints用来控制添加进的组件的显示位置
    private GridBagConstraints mConstraints = new GridBagConstraints();

    public FindViewByIdDialog(@NotNull final Editor editor, @NotNull final PsiClass psiClass,
                              @NotNull final List<ViewWidgetElement> viewWidgetElements, @NotNull final String layoutFileName) {
        mEditor = editor;
        mLayoutFileName = layoutFileName;
        mViewWidgetElements = viewWidgetElements;
        mClass = psiClass;
        mElementSize = mViewWidgetElements.size();
        initExist();
        initContentPanel();
    }

    public FindViewByIdDialog() {
        prepareUI();
    }

    private void prepareUI() {
        setTitle(DIALOG_TITLE);

        // 整个布局 panel
        JPanel contentPane = new JPanel(new GridBagLayout());

        JPanel topLabelsPanel = createTopLabelsPanel();
        GridBagConstraints topLabelsGbc = new GridBagConstraints();
        // 使组件水平填满其显示区域
        topLabelsGbc.fill = GridBagConstraints.HORIZONTAL;
        // 设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        topLabelsGbc.gridwidth = 0;
        // 第几列
        topLabelsGbc.gridx = 0;
        // 第几行
        topLabelsGbc.gridy = 0;
        // 行拉伸0不拉伸，1完全拉伸
        topLabelsGbc.weightx = 1;
        // 列拉伸0不拉伸，1完全拉伸
        topLabelsGbc.weighty = 1;
        contentPane.add(topLabelsPanel, topLabelsGbc);


        JPanel optionsPanel = createOptionsPanel();
        GridBagConstraints optionsGbc = new GridBagConstraints();
        optionsGbc.fill = GridBagConstraints.HORIZONTAL;
        optionsGbc.gridwidth = 0;
        optionsGbc.gridx = 0;
        optionsGbc.gridy = 2;
        optionsGbc.weightx = 0;
        optionsGbc.weighty = 1;
        contentPane.add(optionsPanel, optionsGbc);


        JPanel bottomButtonsPanel = createBottomButtonsPanel();
        GridBagConstraints bottomButtonsGbc = new GridBagConstraints();
        bottomButtonsGbc.anchor = GridBagConstraints.LINE_END;
        bottomButtonsGbc.gridx = 2;
        bottomButtonsGbc.gridy = 3;
        bottomButtonsGbc.weightx = 1;
        contentPane.add(bottomButtonsPanel, bottomButtonsGbc);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(mConfirmButton);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancelDialog();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> cancelDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }


    /**
     * 判断已存在的变量，设置全选
     * 判断onclick是否写入
     */
    private void initExist() {
        // 判断是否已存在的变量
        boolean isFindViewByIdCodeExist = false;
        // 判断是否已存在setOnClickListener
        boolean isClickExist = false;
        // 判断是否存在case R.id.id:
        boolean isCaseExist = false;
        // 获取initView方法的内容
        PsiStatement[] statements = PlatformUtils.getMethodStatements(mClass, WidgetFieldCreator.METHOD_NAME_INIT_VIEW);
        PsiElement[] onClickStatement = Util.getOnClickStatement(mClass);
        for (ViewWidgetElement viewWidgetElement : mViewWidgetElements) {
            if (statements != null) {
                for (PsiStatement statement : statements) {
                    if (statement.getText().contains("findViewById(" + viewWidgetElement.getFullViewId() + ");")) {
                        isFindViewByIdCodeExist = true;
                        break;
                    } else {
                        isFindViewByIdCodeExist = false;
                    }
                }
                String setOnClickListener = viewWidgetElement.getFieldName() + ".setOnClickListener(this);";
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
                String cass = "case " + viewWidgetElement.getFullViewId() + ":";
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
            if (isFindViewByIdCodeExist) {
                // 已存在的变量设置checkbox为false
                viewWidgetElement.setEnable(false);
                mElementSize--;
                if (viewWidgetElement.isClickEnable() && (!isClickExist || !isCaseExist)) {
                    viewWidgetElement.setClickable(true);
                    viewWidgetElement.setEnable(true);
                    mElementSize++;
                }
            }
        }
        mCheckAllViewWidgetsCheckBox.setSelected(mElementSize == mViewWidgetElements.size());
    }


    private JPanel createTopLabelsPanel() {
        JPanel topLabelsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        topLabelsPanel.setBorder(new EmptyBorder(IN_GROUP_VERTICAL_GAP, LEFT_INSET, OUT_GROUP_VERTICAL_GAP, RIGHT_INSET));

        JLabel viewNameLabel = new JLabel("View 类型");
        viewNameLabel.setHorizontalAlignment(JLabel.LEFT);

        JLabel viewIdLabel = new JLabel("View id");
        viewIdLabel.setHorizontalAlignment(JLabel.LEFT);

        JLabel onClickLabel = new JLabel("OnClick");
        onClickLabel.setHorizontalAlignment(JLabel.LEFT);

        JLabel viewFieldNameLabel = new JLabel("目标成员变量名");
        viewFieldNameLabel.setHorizontalAlignment(JLabel.LEFT);

        topLabelsPanel.add(viewNameLabel);
        topLabelsPanel.add(viewIdLabel);
        topLabelsPanel.add(onClickLabel);
        topLabelsPanel.add(viewFieldNameLabel);

        return topLabelsPanel;
    }


    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setBorder(new EmptyBorder(IN_GROUP_VERTICAL_GAP, LEFT_INSET, OUT_GROUP_VERTICAL_GAP, RIGHT_INSET));

        mCheckAllViewWidgetsCheckBox.addActionListener(this);
        optionsPanel.add(mCheckAllViewWidgetsCheckBox);

        optionsPanel.add(mRootViewCheckBox);
        optionsPanel.add(mRootViewNameTextField);

        return optionsPanel;
    }


    private JPanel createBottomButtonsPanel() {

        mConfirmButton.addActionListener(this);

        JButton cancelButton = new JButton(CMD_CANCEL);
        cancelButton.addActionListener(this);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EmptyBorder(IN_GROUP_VERTICAL_GAP, LEFT_INSET, IN_GROUP_VERTICAL_GAP, RIGHT_INSET));

        GroupLayout buttonsGroupLayout = new GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsGroupLayout);

        buttonsGroupLayout.setAutoCreateGaps(true);
        buttonsGroupLayout.setAutoCreateContainerGaps(true);
        buttonsGroupLayout.setHorizontalGroup(buttonsGroupLayout.createSequentialGroup()
                .addComponent(cancelButton)
                .addComponent(mConfirmButton));
        buttonsGroupLayout.setVerticalGroup(buttonsGroupLayout.createParallelGroup()
                .addComponent(cancelButton)
                .addComponent(mConfirmButton));
        buttonsGroupLayout.linkSize(SwingConstants.HORIZONTAL, cancelButton, mConfirmButton);
        buttonsGroupLayout.linkSize(SwingConstants.VERTICAL, cancelButton, mConfirmButton);

        return buttonsPanel;
    }


    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        mContentJPanel.removeAll();
        // 设置内容
        for (int i = 0; i < mViewWidgetElements.size(); i++) {
            ViewWidgetElement element = mViewWidgetElements.get(i);
            IdBean itemJPanel = new IdBean(new GridLayout(1, 4, 10, 10),
                    new EmptyBorder(5, 10, 5, 10),
                    new JCheckBox(element.getViewName()),
                    new JLabel(element.getId()),
                    new JCheckBox(),
                    new JTextField(element.getFieldName()),
                    element.isEnable(),
                    element.isClickable(),
                    element.isClickEnable());
            // 监听
            itemJPanel.setEnableActionListener(enableCheckBox -> element.setEnable(enableCheckBox.isSelected()));
            itemJPanel.setClickActionListener(clickCheckBox -> element.setClickable(clickCheckBox.isSelected()));
            itemJPanel.setFieldFocusListener(fieldJTextField -> element.setFieldName(fieldJTextField.getText()));
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
       mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 1;
        mConstraints.weightx = 1;
        mConstraints.weighty = 1;
        mLayout.setConstraints(jScrollPane, mConstraints);
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
                generateFindViewById(mRootViewCheckBox.isSelected(), mRootViewNameTextField.getText());
                break;
            case CMD_CANCEL:
                cancelDialog();
                break;
            case CMD_CHECK_ALL_VIEW_WIDGETS:
                // 刷新
                for (ViewWidgetElement mElement : mViewWidgetElements) {
                    mElement.setEnable(mCheckAllViewWidgetsCheckBox.isSelected());
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
     * 生成 findViewById 代码
     *
     * @param isRootViewFind 是否是通过 rootView 来 findViewById
     * @param rootViewName           rootView 变量名
     */
    private void generateFindViewById(final boolean isRootViewFind, @Nullable final String rootViewName) {
        String validRootViewName = TextUtils.isBlank(rootViewName)? ROOT_VIEW_NAME_DEFAULT : rootViewName.replace(" ", "");
        new WidgetFieldCreator(mEditor, mClass, mViewWidgetElements, isRootViewFind, validRootViewName)
                .execute();
    }


    // FIXME: 17/8/3 UI test code
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FindViewByIdDialog dialog = new FindViewByIdDialog();
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        });
    }


}
