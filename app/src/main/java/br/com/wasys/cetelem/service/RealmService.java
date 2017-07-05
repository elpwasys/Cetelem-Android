package br.com.wasys.cetelem.service;

import org.apache.commons.collections4.CollectionUtils;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by pascke on 04/07/17.
 */

public class RealmService {

    public static <T extends RealmObject> Long getNextId(Class<T> clazz) {
        Long id = 1l;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<T> results = realm.where(clazz).findAll();
        if (CollectionUtils.isNotEmpty(results)) {
            long max = results.max("id").longValue();
            if (max > 0) {
                id = (max + 1);
            }
        }
        realm.close();
        return id;
    }
}
