package br.com.wasys.cetelem;

import android.Manifest;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by pascke on 05/09/16.
 */
public class Permission {

    public static final String[] PHONE = {
            Manifest.permission.READ_PHONE_STATE
    };
    public static final String[] STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String[] LOCATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static String[] merge(String[] permitions, String... permition) {
        return ArrayUtils.addAll(permitions, permition);
    }
}
