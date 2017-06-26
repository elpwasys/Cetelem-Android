package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;

import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppIntegerEditText extends AppEditText {

    public AppIntegerEditText(Context context) {
        super(context);
        configure();
    }

    public AppIntegerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure();
    }

    public AppIntegerEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure();
    }

    private void configure() {
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public boolean isValid() {
        if (isEmpty()) {
            return true;
        }
        Integer value = FieldUtils.getValue(Integer.class, this);
        return value != null;
    }
}
