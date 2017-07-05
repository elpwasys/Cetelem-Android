package br.com.wasys.cetelem;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import br.com.wasys.library.utils.PreferencesUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by pascke on 03/08/16.
 */
public class Application extends br.com.wasys.library.Application {

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Realm.init(this);
        /*RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("data.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);*/
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build()
        );
    }

    public static Application getInstance() {
        return instance;
    }
}
