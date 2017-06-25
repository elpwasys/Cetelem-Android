package br.com.wasys.library.utils;

import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

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

    public static void setText(TextView textView, Object value) {
        if (value == null) {
            textView.setText(null);
        }
        else {
            if (value instanceof Date) {
                textView.setText(DateUtils.format((Date) value, DateUtils.DateType.DATE_BR.getPattern()));
            }
            else if (value instanceof Double) {
                textView.setText(NumberUtils.format((Double) value));
            }
            else {
                String text = String.valueOf(value);
                if (StringUtils.isNotBlank(text)) {
                    textView.setText(text);
                }
            }
        }
    }

    public static void setText(EditText editText, String value) {
        setText((TextView) editText, value);
    }

    public static void setText(TextView textView, Date value, DateUtils.DateType dateType) {
        if (value == null) {
            textView.setText(null);
        }
        else {
            textView.setText(DateUtils.format((Date) value, dateType.getPattern()));
        }
    }
}
