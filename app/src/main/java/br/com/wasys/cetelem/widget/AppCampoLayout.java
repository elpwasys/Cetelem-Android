package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.CampoModel;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppCampoLayout extends TextInputLayout {

    private String mNome;
    private CampoModel mCampo;
    private AppEditText mEditText;

    public AppCampoLayout(Context context) {
        super(context);
    }

    public AppCampoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppCampoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void configurar(CampoModel campo) {

        removeAllViews();

        mCampo = campo;
        mEditText = null;

        mNome = StringUtils.capitalize(mCampo.nome.toLowerCase());

        Context context = getContext();
        CampoModel.Tipo tipo = campo.tipo;

        if (CampoModel.Tipo.DATA.equals(tipo)) {
            mEditText = new AppDateEditText(context);
        } else if (CampoModel.Tipo.EMAIL.equals(tipo)) {
            mEditText = new AppEmailEditText(context);
            mEditText.setLines(1);
            mEditText.setSingleLine(true);
        } else if (CampoModel.Tipo.MOEDA.equals(tipo)) {
            mEditText = new AppMoneyEditText(context);
        } else if (CampoModel.Tipo.INTEIRO.equals(tipo)) {
            mEditText = new AppIntegerEditText(context);
        } else if (CampoModel.Tipo.TEXTO_LONGO.equals(tipo)) {
            mEditText = new AppEditText(context);
        } else {
            mEditText = new AppEditText(context);
            mEditText.setLines(1);
            mEditText.setSingleLine(true);
        }

        mEditText.setHint(mNome);

        Integer maxLength = campo.tamanhoMaximo;
        if (maxLength != null) {
            mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
        }

        mEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        addView(mEditText);
    }

    public String getValue() {
        if (!mEditText.isValid()) {
            return null;
        }
        return FieldUtils.getValue(mEditText);
    }

    public boolean validate() {
        Context context = getContext();
        this.setError(null);
        this.setErrorEnabled(false);
        if (mCampo.obrigatorio) {
            if (mEditText.isEmpty()) {
                this.setErrorEnabled(true);
                this.setError(context.getString(R.string.msg_required_field, mNome));
                return false;
            }
        }
        if (!mEditText.isValid()) {
            this.setErrorEnabled(true);
            this.setError(context.getString(R.string.msg_invalid_field, mNome));
            return false;
        }
        return true;
    }
}
