package br.com.wasys.cetelem;

import br.com.wasys.library.utils.PreferencesUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by pascke on 03/08/16.
 */
public class Application extends br.com.wasys.library.Application {

    private static final String TOKEN_PREFERENCES_KEY = Application.class.getName() + ".token";

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("data.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    public static String getToken() {
        return PreferencesUtils.get(TOKEN_PREFERENCES_KEY);
    }

    public static void setAuthorization(String authorization) {
        PreferencesUtils.put(TOKEN_PREFERENCES_KEY, authorization);
    }
}
