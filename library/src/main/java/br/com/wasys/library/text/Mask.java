package br.com.wasys.library.text;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by pascke on 09/05/16.
 */
public abstract class Mask {

    public static String unmask(String value) {
        return value
            .replaceAll("[.]", "").replaceAll("[-]", "")
            .replaceAll("[/]", "").replaceAll("[(]", "")
            .replaceAll("[)]", "");
    }

    public static TextWatcher insert(final String mask, final EditText editText) {
        TextWatcher textWatcher = new TextWatcher() {
            boolean isUpdating;
            String old = "";
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                String text = Mask.unmask(String.valueOf(sequence));
                String mascara = "";
                if (isUpdating) {
                    old = text;
                    isUpdating = false;
                    return;
                }
                int i = 0;
                for (char m : mask.toCharArray()) {
                    if (m != '#' && text.length() > old.length()) {
                        mascara += m;
                        continue;
                    }
                    try {
                        mascara += text.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                editText.setText(mascara);
                editText.setSelection(mascara.length());
            }
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
            }
            public void afterTextChanged(Editable editable) {
            }
        };
        editText.addTextChangedListener(textWatcher);
        return textWatcher;
    }
}