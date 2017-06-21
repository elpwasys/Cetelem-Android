package br.com.wasys.cetelem;

import android.Manifest;

/**
 * Created by pascke on 05/09/16.
 */
public interface Permission {

    static final String[] PHONE = { Manifest.permission.READ_PHONE_STATE };
    static final String[] LOCATION = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
}
