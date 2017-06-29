package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.model.CampoModel;
import br.com.wasys.library.utils.AndroidUtils;

/**
 * Created by pascke on 28/06/17.
 */

public class AppRadioLayout extends LinearLayout {

    private String mNome;
    private CampoModel mCampo;

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

        mCampo = campo;
        mNome = StringUtils.capitalize(mCampo.nome.toLowerCase());

        setOrientation(HORIZONTAL);

        Context context = getContext();

        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        textView.setText(mNome);

        TextViewCompat.setTextAppearance(textView, android.R.style.TextAppearance_Small);

        addView(textView);

        Switch aSwitch = new Switch(context);
        aSwitch.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        aSwitch.setMinWidth(AndroidUtils.toPixels(context, 100f));

        addView(aSwitch);
    }
}
