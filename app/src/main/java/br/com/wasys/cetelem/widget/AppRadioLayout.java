package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.model.CampoModel;

/**
 * Created by pascke on 28/06/17.
 */

public class AppRadioLayout extends LinearLayout {

    private String mNome;
    private CampoModel mCampo;
    private RadioGroup mRadioGroup;

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

    public void configurar(CampoModel campo) {

        removeAllViews();
        setOrientation(VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mCampo = campo;
        mNome = StringUtils.capitalize(mCampo.nome.toLowerCase());

        Context context = getContext();

        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(mNome);

        TextViewCompat.setTextAppearance(textView, android.R.style.TextAppearance_Small);

        addView(textView);

        mRadioGroup = new RadioGroup(context);
        mRadioGroup.setTag(campo.nome);
        mRadioGroup.setOrientation(HORIZONTAL);
        mRadioGroup.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        String opcoes = campo.opcoes;
        if (StringUtils.isNotBlank(opcoes)) {
            String[] split = opcoes.split(",");
            for (String opcao : split) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                radioButton.setTag(opcao);
                radioButton.setText(opcao);
                mRadioGroup.addView(radioButton);
            }
        }

        addView(mRadioGroup);
    }

    public boolean validate() {
        String value = getValue();
        return StringUtils.isNotBlank(value);
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
}
