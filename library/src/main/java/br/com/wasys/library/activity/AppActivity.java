package br.com.wasys.library.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import br.com.wasys.library.widget.AppProgress;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by pascke on 03/08/16.
 */
public abstract class AppActivity extends AppCompatActivity {

    private Looper mLooper;
    private AppProgress mProgress;
    private HandlerThread mHandlerThread;

    public String getTag() {
        return getClass().getSimpleName();
    }

    /**
     * Android lifecycle
     ************************************************/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Lifecycle", String.format("Starting %1$s...", getTag()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Lifecycle", String.format("Resuming %1$s...", getTag()));
    }

    @Override
    protected void onPause() {
        hideProgress();
        Log.i("Lifecycle", String.format("Pausing %1$s...", getTag()));
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Lifecycle", String.format("Stoping %1$s...", getTag()));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Lifecycle", String.format("Restarting %1$s...", getTag()));
    }

    @Override
    protected void onDestroy() {
        Log.i("Lifecycle", String.format("Destroying %1$s...", getTag()));
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        super.onDestroy();
    }

    protected void showProgress() {
        if (mProgress == null) {
            mProgress = new AppProgress(this);
        }
        Log.i(getTag(), "Showing progress...");
        mProgress.show();
    }

    protected void hideProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            Log.i(getTag(), "Hiding progress...");
            mProgress.dismiss();
        }
    }

    protected Observable prepare(Observable observable) {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.from(getLooper()));
    }

    protected Looper getLooper() {
        if (mLooper == null) {
            if (mHandlerThread == null) {
                mHandlerThread = new HandlerThread(getTag(), Process.THREAD_PRIORITY_BACKGROUND);
                mHandlerThread.start();
            }
            mLooper = mHandlerThread.getLooper();
        }
        return mLooper;
    }

    protected void handle(Throwable throwable) {
        throwable.printStackTrace();
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

    protected boolean grantedResults(int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    protected boolean checkedSelfPermission(String[] permissions) {
        boolean checked = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                checked = false;
                break;
            }
        }
        return checked;
    }
}