package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.wasys.cetelem.model.CampoModel;
import br.com.wasys.library.utils.AndroidUtils;
import okhttp3.RequestBody;

/**
 * Created by pascke on 28/06/17.
 */

public class AppSwitch extends LinearLayout {

    private String mNome;
    private String[] mOptions;
    private CampoModel mCampo;

    public AppSwitch(Context context) {
        super(context);
    }

    public AppSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void configurar(CampoModel campo) {

        removeAllViews();

        setOrientation(VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Context context = getContext();

        mCampo = campo;

        setOptions(campo.opcoes);
        mNome = StringUtils.capitalize(mCampo.nome.toLowerCase());

        Switch aSwitch = new Switch(context);
        aSwitch.setTag(mNome);
        aSwitch.setText(mNome);
        aSwitch.setMinWidth(AndroidUtils.toPixels(context, 100f));

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int marginLeft = AndroidUtils.toPixels(context, 4f);
        params.setMargins(marginLeft, 0, 0, 0);

        aSwitch.setLayoutParams(params);

        TextViewCompat.setTextAppearance(aSwitch, android.R.style.TextAppearance_Small);

        addView(aSwitch);
    }

    private void setOptions(String options) {
        if (isSwitch(options)) {
            String text = StringUtils.replacePattern(options, "\\s", "");
            mOptions = text.split(",");
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Switch aSwitch = (Switch) findViewWithTag(mNome);
        if (aSwitch != null) {
            aSwitch.setEnabled(enabled);
        }
    }

    public String getValue() {
        Switch aSwitch = (Switch) findViewWithTag(mNome);
        boolean checked = aSwitch.isChecked();
        if (checked) {
            String expression = "(?i)sim";
            if (mOptions[0].matches(expression)) {
                return mOptions[0];
            } else if (mOptions[1].matches(expression)) {
                return mOptions[1];
            }
        } else {
            String expression = "(?i)nao";
            if (StringUtils.stripAccents(mOptions[0]).matches(expression)) {
                return mOptions[0];
            } else if (StringUtils.stripAccents(mOptions[1]).matches(expression)) {
                return mOptions[1];
            }
        }
        return null;
    }

    public static boolean isSwitch(String opcoes) {
        if (StringUtils.isNotBlank(opcoes)) {
            String text = StringUtils.replacePattern(opcoes, "\\s", "");
            text = StringUtils.stripAccents(text);
            Pattern pattern = Pattern.compile("((?i)(sim,nao)|(nao,sim))");
            Matcher matcher = pattern.matcher(text);
            return matcher.matches();
        }
        return false;
    }


    public void setValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            boolean checked = false;
            if (value.matches("(?i)sim")) {
                checked = true;
            }
            Switch aSwitch = (Switch) findViewWithTag(mNome);
            aSwitch.setChecked(checked);
        }
    }
}
