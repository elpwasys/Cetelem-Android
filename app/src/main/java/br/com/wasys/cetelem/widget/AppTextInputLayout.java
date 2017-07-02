package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.CampoModel;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppTextInputLayout extends TextInputLayout {

    private String mNome;
    private CampoModel mCampo;
    private AppEditText mEditText;

    public AppTextInputLayout(Context context) {
        super(context);
    }

    public AppTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
            mEditText = new AppNumberEditText(context);
        } else if (CampoModel.Tipo.TEXTO_LONGO.equals(tipo)) {
            mEditText = new AppEditText(context);
        } else {
            mEditText = new AppEditText(context);
            mEditText.setLines(1);
            mEditText.setSingleLine(true);
        }

        mEditText.setHint(mNome);
        FieldUtils.setText(mEditText, mCampo.valor);

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
        if (BooleanUtils.isTrue(mCampo.obrigatorio)) {
            if (mEditText.isEmpty()) {
                this.setErrorEnabled(true);
                this.setError(context.getString(R.string.msg_required_field, mNome));
                return false;
            }
        }
        Integer tamanhoMinimo = mCampo.tamanhoMinimo;
        if (tamanhoMinimo != null) {
            String value = FieldUtils.getValue(mEditText);
            if (StringUtils.length(value) < tamanhoMinimo) {
                this.setErrorEnabled(true);
                this.setError(context.getString(R.string.msg_min_size_field, mNome, tamanhoMinimo));
                return false;
            }
        }
        Integer tamanhoMaximo = mCampo.tamanhoMaximo;
        if (tamanhoMaximo != null) {
            String value = FieldUtils.getValue(mEditText);
            if (StringUtils.length(value) > tamanhoMaximo) {
                this.setErrorEnabled(true);
                this.setError(context.getString(R.string.msg_max_size_field, mNome, tamanhoMaximo));
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
