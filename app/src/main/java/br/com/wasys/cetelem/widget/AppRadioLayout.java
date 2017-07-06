package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.CampoModel;
import br.com.wasys.library.utils.AndroidUtils;

/**
 * Created by pascke on 28/06/17.
 */

public class AppRadioLayout extends LinearLayout {

    private String mNome;
    private String[] mValues;
    private CampoModel mCampo;

    private TextView mErrorView;
    private RadioGroup mRadioGroup;

    private static final String TAG_VIEW_ERROR = "TagErrorView";

    public AppRadioLayout(Context context) {
        super(context);
    }

    public AppRadioLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppRadioLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppRadioLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mRadioGroup != null) {
            int childCount = mRadioGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(i);
                radioButton.setEnabled(enabled);
            }
        }
    }

    public void configurar(CampoModel campo) {

        removeAllViews();
        setOrientation(VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mCampo = campo;
        mNome = StringUtils.capitalize(mCampo.nome.toLowerCase());

        Context context = getContext();

        TextView textView = new TextView(context);
        LayoutParams textViewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.setMargins(AndroidUtils.toPixels(context, 4f), 0, 0, AndroidUtils.toPixels(context, 8f));
        textView.setLayoutParams(textViewLayoutParams);
        textView.setText(mNome);

        TextViewCompat.setTextAppearance(textView, android.R.style.TextAppearance_Small);

        addView(textView);

        mRadioGroup = new RadioGroup(context);
        mRadioGroup.setTag(campo.nome);
        mRadioGroup.setOrientation(VERTICAL);
        LayoutParams radioGroupLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        radioGroupLayoutParams.setMargins(AndroidUtils.toPixels(context, -4f), 0, 0, 0);
        mRadioGroup.setLayoutParams(radioGroupLayoutParams);
        String opcoes = campo.opcoes;
        if (StringUtils.isNotBlank(opcoes)) {
            mValues = opcoes.split(",");
            for (String opcao : mValues) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                radioButton.setTag(opcao);
                radioButton.setText(opcao);
                mRadioGroup.addView(radioButton);
            }
        }

        addView(mRadioGroup);

        mErrorView = new TextView(context);
        mErrorView.setTag(TAG_VIEW_ERROR);
        LayoutParams errorLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        errorLayoutParams.setMargins(AndroidUtils.toPixels(context, 4f), AndroidUtils.toPixels(context, 4f), 0, 0);
        mErrorView.setLayoutParams(errorLayoutParams);
        mErrorView.setVisibility(View.GONE);

        TextViewCompat.setTextAppearance(mErrorView, android.support.design.R.style.TextAppearance_AppCompat_Caption);

        mErrorView.setTextColor(ContextCompat.getColor(
                getContext(), android.support.design.R.color.design_textinput_error_color_light));

        addView(mErrorView);
    }

    public boolean validate() {
        String value = getValue();
        Context context = getContext();
        boolean isValid = StringUtils.isNotBlank(value);
        mErrorView = (TextView) findViewWithTag(TAG_VIEW_ERROR);
        if (isValid) {
            mErrorView.setText(null);
            mErrorView.setVisibility(View.GONE);
        } else {
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(context.getString(R.string.msg_required_field, mNome));
        }
        return isValid;
    }

    public String getValue() {
        if (mRadioGroup != null) {
            int childCount = mRadioGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(i);
                if (radioButton.isChecked()) {
                    return (String) radioButton.getTag();
                }
            }
        }
        return null;
    }

    public void setValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            int index = ArrayUtils.indexOf(mValues, value);
            if (index > -1) {
                RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(index);
                radioButton.setChecked(true);
            }
        }
    }
}
