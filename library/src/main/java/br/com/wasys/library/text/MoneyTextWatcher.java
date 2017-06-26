package br.com.wasys.library.text;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import br.com.wasys.library.utils.NumberUtils;

/**
 * Created by pascke on 22/05/16.
 */
public class MoneyTextWatcher implements TextWatcher {

    private final WeakReference<EditText> weakReference;

    public MoneyTextWatcher(EditText editText) {
        weakReference = new WeakReference<EditText>(editText);
        editText.addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        EditText editText = weakReference.get();
        if (editText == null) {
            return;
        }
        String text = editable.toString();
        editText.removeTextChangedListener(this);
        String replaced = text.replaceAll("[$,.]", "");
        if (StringUtils.isNotBlank(replaced)) {
            BigDecimal parsed = new BigDecimal(replaced).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberUtils.format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
        }
        editText.addTextChangedListener(this);
    }
}
