package view;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.ui.components.JBScrollPane;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Constants;
import utils.PlatformUtils;
import utils.WidgetFieldCreator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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

    private Editor mEditor;
    private List<ViewWidgetElement> mViewWidgetElements;
    // 获取class
    private PsiClass mClass;

    private final JButton mConfirmButton = new JButton(CMD_CONFIRM);

    private final JCheckBox mCheckAllViewWidgetsCheckBox = new JCheckBox(CMD_CHECK_ALL_VIEW_WIDGETS);
    private final JCheckBox mRootViewCheckBox = new JCheckBox(CMD_CHECK_ROOT_VIEW);
    private final JTextField mRootViewNameTextField = new JTextField(ROOT_VIEW_NAME_DEFAULT, ROOT_VIEW_NAME_DEFAULT.length() + 4);

    public FindViewByIdDialog(@NotNull final Editor editor, @NotNull final PsiClass psiClass,
                              @NotNull final List<ViewWidgetElement> viewWidgetElements) {
        mEditor = editor;
        mViewWidgetElements = viewWidgetElements;
        mClass = psiClass;
        prepareUI();
    }

    private void prepareUI() {
        setTitle(DIALOG_TITLE);

        initSomeUIFromData();

        // 整个布局 panel
        JPanel contentPane = new JPanel(new GridBagLayout());

        JPanel topLabelsPanel = createTopLabelsPanel();
        GridBagConstraints topLabelsGbc = new GridBagConstraints();
        // 使组件完全填满其显示区域
        topLabelsGbc.fill = GridBagConstraints.BOTH;
        // 设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        topLabelsGbc.gridwidth = 0;
        // 第几列
        topLabelsGbc.gridx = 0;
        // 第几行
        topLabelsGbc.gridy = 0;
        // 行拉伸0不拉伸，1完全拉伸
        topLabelsGbc.weightx = 1;
        // 列拉伸0不拉伸，1完全拉伸
        topLabelsGbc.weighty = 0;
        contentPane.add(topLabelsPanel, topLabelsGbc);

        refreshViewWidgetElementsPanel();
        GridBagConstraints viewWidgetElementsGbc = new GridBagConstraints();
        viewWidgetElementsGbc.fill = GridBagConstraints.BOTH;
        viewWidgetElementsGbc.gridwidth = 1;
        viewWidgetElementsGbc.gridx = 0;
        viewWidgetElementsGbc.gridy = 1;
        viewWidgetElementsGbc.weightx = 1;
        viewWidgetElementsGbc.weighty = 1;
        mViewWidgetElementsScrollPanel.setBorder(new EmptyBorder(Constants.Dimen.OUT_GROUP_VERTICAL_GAP, Constants.Dimen.LEFT_INSET,
                Constants.Dimen.OUT_GROUP_VERTICAL_GAP, Constants.Dimen.RIGHT_INSET));
        contentPane.add(mViewWidgetElementsScrollPanel, viewWidgetElementsGbc);


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
        bottomButtonsGbc.gridwidth = 0;
        bottomButtonsGbc.gridx = 0; // 此处是整个 UI 对齐的关键
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


    private void initSomeUIFromData() {
        PsiStatement[] statements = PlatformUtils.getMethodStatements(mClass, WidgetFieldCreator.METHOD_NAME_INIT_VIEW);
        if (statements == null) {
            mCheckAllViewWidgetsCheckBox.setSelected(true);
            return;
        }

        boolean isFindViewByIdCodeExist = false;
        for (ViewWidgetElement element : mViewWidgetElements) {
            boolean isElementFindViewByIdCodeExist = false;
            for (PsiStatement statement : statements) {
                if (PlatformUtils.isMethodInvoked(statement, "findViewById", element.getFullViewId())) {
                    isElementFindViewByIdCodeExist = true;
                    isFindViewByIdCodeExist = true;
                    break;
                }
            }

            if (isElementFindViewByIdCodeExist) {
                element.setNeedGenerate(false);
            }

        }
        mCheckAllViewWidgetsCheckBox.setSelected(!isFindViewByIdCodeExist);
    }


    private JPanel createTopLabelsPanel() {
        JPanel topLabelsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        topLabelsPanel.setBorder(new EmptyBorder(Constants.Dimen.IN_GROUP_VERTICAL_GAP, Constants.Dimen.LEFT_INSET,
                Constants.Dimen.OUT_GROUP_VERTICAL_GAP, Constants.Dimen.RIGHT_INSET));

        JLabel viewIdLabel = new JLabel("View Id");
        viewIdLabel.setBorder(new EmptyBorder(0, Constants.Dimen.LEFT_INSET, 0, 0));
        viewIdLabel.setHorizontalAlignment(JLabel.LEFT);

        JLabel viewNameLabel = new JLabel("View 类型");
        viewNameLabel.setHorizontalAlignment(JLabel.LEFT);

        JLabel onClickLabel = new JLabel("OnClick");
        onClickLabel.setHorizontalAlignment(JLabel.LEFT);

        JLabel viewFieldNameLabel = new JLabel("View成员变量名");
        viewFieldNameLabel.setHorizontalAlignment(JLabel.LEFT);

        topLabelsPanel.add(viewIdLabel);
        topLabelsPanel.add(viewNameLabel);
        topLabelsPanel.add(onClickLabel);
        topLabelsPanel.add(viewFieldNameLabel);

        return topLabelsPanel;
    }


    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setBorder(new EmptyBorder(Constants.Dimen.IN_GROUP_VERTICAL_GAP, Constants.Dimen.LEFT_INSET,
                Constants.Dimen.OUT_GROUP_VERTICAL_GAP, Constants.Dimen.RIGHT_INSET));

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
        buttonsPanel.setBorder(new EmptyBorder(Constants.Dimen.IN_GROUP_VERTICAL_GAP, Constants.Dimen.LEFT_INSET,
                Constants.Dimen.IN_GROUP_VERTICAL_GAP, Constants.Dimen.RIGHT_INSET));

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

    private final JPanel mViewWidgetElementsPanel = new JPanel(new GridBagLayout());
    private final GridBagConstraints mViewWidgetElementConstraints = new GridBagConstraints();
    private final JBScrollPane mViewWidgetElementsScrollPanel = new JBScrollPane(mViewWidgetElementsPanel);

    private void refreshViewWidgetElementsPanel() {
        mViewWidgetElementsPanel.removeAll();

        mViewWidgetElementConstraints.fill = GridBagConstraints.HORIZONTAL;
        mViewWidgetElementConstraints.gridwidth = 0;
        mViewWidgetElementConstraints.gridx = 0;
        mViewWidgetElementConstraints.weightx = 1;

        for (int i = 0, size = mViewWidgetElements.size(); i < size; i++) {
            ViewWidgetElement element = mViewWidgetElements.get(i);
            ViewWidgetElementPanel elementPanel = new ViewWidgetElementPanel(element);
            elementPanel.setOnEnableGenerateThisIdChangedListener(element::setNeedGenerate);
            elementPanel.setOnEnableGenerateOnClickChangedListener(element::setGenerateOnClickMethod);
            elementPanel.setOnViewFieldNameChangedListener(viewFieldName -> {
                if (!viewFieldName.isEmpty()) {
                    element.setFieldName(viewFieldName);
                }
            });

            mViewWidgetElementConstraints.gridy = i;
            mViewWidgetElementsPanel.add(elementPanel, mViewWidgetElementConstraints);
        }
        mViewWidgetElementsScrollPanel.revalidate();
    }


    public void showDialog() {
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
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
                for (ViewWidgetElement element : mViewWidgetElements) {
                    element.setNeedGenerate(mCheckAllViewWidgetsCheckBox.isSelected());
                }
                refreshViewWidgetElementsPanel();
                break;
            default:
                break;
        }
    }

    /**
     * 生成 findViewById 代码
     *
     * @param isRootViewFind 是否是通过 rootView 来 findViewById
     * @param rootViewName   rootView 变量名
     */
    private void generateFindViewById(final boolean isRootViewFind, @Nullable final String rootViewName) {
        String validRootViewName = TextUtils.isBlank(rootViewName) ? ROOT_VIEW_NAME_DEFAULT : rootViewName.replace(" ", "");
        new WidgetFieldCreator(mEditor, mClass, mViewWidgetElements, isRootViewFind, validRootViewName)
                .execute();
    }


}
