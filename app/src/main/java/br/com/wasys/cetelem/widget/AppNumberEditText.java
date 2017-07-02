package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;

import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppNumberEditText extends AppEditText {

    public AppNumberEditText(Context context) {
        super(context);
        configure();
    }

    public AppNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure();
    }

    public AppNumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure();
    }

    private void configure() {
        setLines(1);
        setSingleLine(true);
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public String getValue() {
        return FieldUtils.getValue(this);
    }

    public void setValue(String value) {
        FieldUtils.setText(this, value);
    }
}
