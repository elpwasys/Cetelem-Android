package br.com.wasys.library.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by pascke on 19/04/16.
 */
public class FragmentUtils {

    private FragmentUtils() {

    }

    public static void replace(FragmentActivity activity, int id, Fragment fragment) {
        replace(activity, id, fragment, false);
    }

    public static void replace(FragmentActivity activity, int id, Fragment fragment, boolean addToBackStack) {
        replace(activity, id, fragment, null, addToBackStack);
    }

    public static void replace(FragmentActivity activity, int id, Fragment fragment, String tag, boolean addToBackStack) {
        FragmentManager manager = activity.getSupportFragmentManager();
        if (StringUtils.isNotBlank(tag)) {
            Fragment fragmentByTag = manager.findFragmentByTag(tag);
            if (fragmentByTag != null) {
                return;
            }
        }
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(id, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private static FragmentTransaction beginTransaction(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        return manager.beginTransaction();
    }
}
