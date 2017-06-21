package br.com.wasys.library.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Created by pascke on 20/04/16.
 */
public class AndroidUtils {

    private static final String TAG = AndroidUtils.class.getCanonicalName();

    public static String getIMEI(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = manager.getDeviceId();
        return imei;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources resources = context.getResources();
        return resources.getDisplayMetrics();
    }

    public static int getWidthPixels(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics.widthPixels;
    }

    public static int getHeightPixels(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics.heightPixels;
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager pack = context.getPackageManager();
            String pacote = context.getPackageName();
            PackageInfo packageInfo = pack.getPackageInfo(pacote, 0);
            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, ExceptionUtils.getRootCauseMessage(e), e);
            return 0;
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager pack = context.getPackageManager();
            String pacote = context.getPackageName();
            PackageInfo packageInfo = pack.getPackageInfo(pacote, 0);
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, ExceptionUtils.getRootCauseMessage(e), e);
            return null;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        else {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static int toPixels(Context context, float dip) {
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        int px = (int) (dip * scale + 0.5f);
        return px;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void requestFocus(final EditText editText, boolean showKeyboard) {
        editText.requestFocus();
        if (showKeyboard) {
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showKeyboard(editText.getContext(), editText);
                }
            }, 250);
        }
    }

    public static boolean hideKeyboard(Context context, View view) {
        InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(manager != null) {
            boolean b = manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return b;
        }
        return false;
    }

    public static boolean showKeyboard(Context context, View view) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(manager != null) {
            boolean b = manager.showSoftInput(view, 0);
            return b;
        }
        return false;
    }

    public static void closeVirtualKeyBoardOnTouch(final Context context, final View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    IBinder windowToken = view.getWindowToken();
                    manager.hideSoftInputFromWindow(windowToken, 0);
                }
            }
        });
    }
}
