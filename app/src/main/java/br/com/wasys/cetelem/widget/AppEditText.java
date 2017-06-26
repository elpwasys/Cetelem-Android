package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppEditText extends AppCompatEditText {

    public AppEditText(Context context) {
        super(context);
        configure();
    }

    public AppEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure();
    }

    public AppEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure();
    }

    private void configure() {
        Context context = getContext();
        TextViewCompat.setTextAppearance(this, android.R.style.TextAppearance_Small);
        ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.colorPrimary);
        setBackgroundTintList(colorStateList);
    }

    public boolean isValid() {
        return true;
    }

    public boolean isEmpty() {
        String value = FieldUtils.getValue(this);
        return StringUtils.isBlank(value);
    }
}
