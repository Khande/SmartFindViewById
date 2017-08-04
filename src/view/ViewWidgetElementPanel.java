package view;

import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Khande on 17/8/4.
 */
public class ViewWidgetElementPanel extends JPanel {

    private OnEnableGenerateThisIdChangedListener mOnEnableGenerateThisIdChangedListener;
    private OnEnableGenerateOnClickChangedListener mOnEnableGenerateOnClickChangedListener;
    private OnViewFieldNameChangedListener mOnViewFieldNameChangedListener;


    private JCheckBox mEnableGenerateThisIdCheckbox;
    private JLabel mViewNameLabel;
    private JCheckBox mGenerateOnClickCheckBox;
    private JTextField mViewFieldNameTextField;

    public ViewWidgetElementPanel(@NotNull final ViewWidgetElement element) {

        initChildrenComponents(element);

        setLayout(new GridLayout(1, 4, 10, 10));
        setBorder(new EmptyBorder(Constants.Dimen.IN_GROUP_VERTICAL_GAP, 0, Constants.Dimen.IN_GROUP_VERTICAL_GAP, 0));
        add(mEnableGenerateThisIdCheckbox);
        add(mViewNameLabel);
        add(mGenerateOnClickCheckBox);
        add(mViewFieldNameTextField);

    }


    public void setOnEnableGenerateThisIdChangedListener(OnEnableGenerateThisIdChangedListener listener) {
       mOnEnableGenerateThisIdChangedListener = listener;
    }

    public void setOnEnableGenerateOnClickChangedListener(OnEnableGenerateOnClickChangedListener listener) {
        mOnEnableGenerateOnClickChangedListener = listener;
    }

    public void setOnViewFieldNameChangedListener(OnViewFieldNameChangedListener listener) {
        mOnViewFieldNameChangedListener = listener;
    }

    private void initChildrenComponents(@NotNull final ViewWidgetElement element) {
        mEnableGenerateThisIdCheckbox = new JCheckBox(element.getId());
        mEnableGenerateThisIdCheckbox.setSelected(element.isNeedGenerate());

        mViewNameLabel = new JLabel(element.getViewName());
        mGenerateOnClickCheckBox = new JCheckBox("", element.isGenerateOnClickMethod());

        mViewFieldNameTextField = new JTextField(element.getFieldName());

        mEnableGenerateThisIdCheckbox.addActionListener(e -> {
            boolean enabled = mEnableGenerateThisIdCheckbox.isSelected();
            mViewFieldNameTextField.setEnabled(enabled);
            if (mOnEnableGenerateThisIdChangedListener != null) {
                mOnEnableGenerateThisIdChangedListener.onEnableGenerateThisIdChanged(enabled);
            }
        });

        mGenerateOnClickCheckBox.addActionListener(e -> {
            if (mOnEnableGenerateOnClickChangedListener != null) {
                boolean enabled = mGenerateOnClickCheckBox.isSelected();
                mOnEnableGenerateOnClickChangedListener.onEnableGenerateOnClickChanged(enabled);
            }
        });

        mViewFieldNameTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (mOnViewFieldNameChangedListener != null) {
                    String fieldText = mViewFieldNameTextField.getText();
                    if (TextUtils.isBlank(fieldText)) {
                        fieldText = "";
                    }
                    mOnViewFieldNameChangedListener.onViewFieldNameChanged(fieldText.replace(" ", ""));
                }
            }
        });


    }


    public interface OnEnableGenerateThisIdChangedListener {
        void onEnableGenerateThisIdChanged(final boolean enabled);
    }


    public interface OnEnableGenerateOnClickChangedListener {
        void onEnableGenerateOnClickChanged(final boolean enabled);
    }


    public interface OnViewFieldNameChangedListener {
        void onViewFieldNameChanged(@NotNull final String viewFieldName);
    }


}
