package br.com.wasys.library.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by pascke on 20/09/16.
 */

public class AppAlertDialog extends Dialog {

    private int mIcon;
    private String mTitle;
    private String mMessage;

    private AppAlertDialog(Builder builder) {
        super(builder.context);
        mIcon = builder.icon;
        mTitle = builder.title;
        mMessage = builder.message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setTitle("???");
    }

    public static class Builder {
        private int icon;
        private String title;
        private String message;
        private Context context;
        public Builder(Context context) {
            this.context = context;
        }
        public Builder icon(@DrawableRes int icon) {
            this.icon = icon;
            return this;
        }
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        public Builder title(@StringRes int title) {
            return title(context.getString(title));
        }
        public Builder message(@StringRes int message) {
            return message(context.getString(message));
        }
        public AppAlertDialog build() {
            return new AppAlertDialog(this);
        }
    }
}