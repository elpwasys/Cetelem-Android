package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.ValidatorUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppEmailEditText extends AppEditText {

    public AppEmailEditText(Context context) {
        super(context);
        configure();
    }

    public AppEmailEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure();
    }

    public AppEmailEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure();
    }

    private void configure() {
        setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    @Override
    public boolean isValid() {
        if (isEmpty()) {
            return true;
        }
        String value = FieldUtils.getValue(this);
        return ValidatorUtils.isValidEmail(value);
    }
}