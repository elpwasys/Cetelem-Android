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
        replace(activity, id, fragment, null);
    }

    public static void replace(FragmentActivity activity, int id, Fragment fragment, String backStackName) {
        FragmentTransaction transaction = beginTransaction(activity);
        transaction.replace(id, fragment);
        if (StringUtils.isNotBlank(backStackName)) {
            transaction.addToBackStack(backStackName);
        }
        transaction.commit();
    }

    public static void popAllBackStackImmediate(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static boolean popBackStackImmediate(FragmentActivity activity, String backStackName) {
        FragmentManager manager = activity.getSupportFragmentManager();
        return manager.popBackStackImmediate(backStackName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    /*private void removeByTag(FragmentActivity activity, String tag) {
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(tag);
        manager.popBackStack();
        if (fragment != null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragment);
            manager.popBackStack();
        }
    }*/

    private static FragmentTransaction beginTransaction(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        return manager.beginTransaction();
    }
}
