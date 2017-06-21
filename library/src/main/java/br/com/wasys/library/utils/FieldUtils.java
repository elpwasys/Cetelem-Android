package br.com.wasys.library.utils;

import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by pascke on 02/08/16.
 */
public class FieldUtils {

    public static String getValue(TextView textView) {
        CharSequence text = textView.getText();
        if (StringUtils.isNotBlank(text)) {
            return String.valueOf(text);
        }
        return null;
    }

    public static String getValue(EditText editText) {
        Editable editable = editText.getText();
        String text = String.valueOf(editable);
        if (StringUtils.isNotBlank(text)) {
            return text;
        }
        return null;
    }

    public static <T> T getValue(Class<T> clazz, EditText editText) {
        T value = null;
        String text = getValue(editText);
        if (StringUtils.isNotBlank(text)) {
            value = TypeUtils.parse(clazz, text);
        }
        return value;
    }
}
