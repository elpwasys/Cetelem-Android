package br.com.wasys.cetelem.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import br.com.wasys.cetelem.R;
import br.com.wasys.library.utils.PreferencesUtils;

public class SplashActivity extends CetelemActivity {

    private Handler mHandler = new Handler();

    public static Intent newIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 2000);
    }

    private void start() {
        startActivity(LoginActivity.create(this));
        finish();
    }
}
