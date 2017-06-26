package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import java.lang.reflect.Field;

import br.com.wasys.library.text.Mask;
import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.ValidatorUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppCpfEditText extends AppIntegerEditText {

    public AppCpfEditText(Context context) {
        super(context);
        configure();
    }

    public AppCpfEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure();
    }

    public AppCpfEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure();
    }

    private void configure() {
        Mask.insert("###.###.###-##", this);
    }

    @Override
    public boolean isValid() {
        if (isEmpty()) {
            return true;
        }
        String value = FieldUtils.getValue(this);
        return ValidatorUtils.isValidCpf(value);
    }
}
