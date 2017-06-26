package br.com.wasys.library.fragment;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import br.com.wasys.library.widget.AppProgress;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by pascke on 03/08/16.
 */
public abstract class AppFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Looper mLooper;
    private AppProgress mProgress;
    private HandlerThread mHandlerThread;

    protected Context getBaseContext() {
        FragmentActivity activity = getActivity();
        return activity.getBaseContext();
    }

    private String getTag2() {
        return getClass().getSimpleName();
    }

    protected void showProgress() {
        if (mProgress == null) {
            mProgress = new AppProgress(getActivity());
        }
        Log.i(getTag2(), "Showing progress...");
        mProgress.show();
    }

    protected void hideProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            Log.i(getTag2(), "Hiding progress...");
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
                mHandlerThread = new HandlerThread(getTag2(), Process.THREAD_PRIORITY_BACKGROUND);
                mHandlerThread.start();
            }
            mLooper = mHandlerThread.getLooper();
        }
        return mLooper;
    }

    protected void handle(Throwable throwable) {
        throwable.printStackTrace();
        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }
}