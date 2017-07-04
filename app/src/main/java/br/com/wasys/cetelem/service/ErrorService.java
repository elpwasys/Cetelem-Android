package br.com.wasys.cetelem.service;

import java.util.List;

import br.com.wasys.cetelem.model.ErrorModel;
import br.com.wasys.cetelem.realm.Error;
import br.com.wasys.library.service.Service;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class ErrorService extends Service {

    public static boolean contains(String reference, ErrorModel.Action action, ErrorModel.Generator generator) throws Throwable {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Error> errors = realm.where(Error.class)
                    .equalTo("reference", reference)
                    .equalTo("action", action.name())
                    .equalTo("generator", generator.name())
                    .findAll();
            return !errors.isEmpty();
        } finally {
            realm.close();
        }
    }

    public static List<ErrorModel> find(String reference, ErrorModel.Action action, ErrorModel.Generator generator) throws Throwable {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Error> errors = realm.where(Error.class)
                    .equalTo("reference", reference)
                    .equalTo("action", action.name())
                    .equalTo("generator", generator.name())
                    .findAll();
            return ErrorModel.from(errors);
        } finally {
            realm.close();
        }
    }

    public static class Async {
        public static Observable<List<ErrorModel>> find(final String reference, final ErrorModel.Action action, final ErrorModel.Generator generator) {
            return Observable.create(new Observable.OnSubscribe<List<ErrorModel>>() {
                @Override
                public void call(Subscriber<? super List<ErrorModel>> subscriber) {
                    try {
                        List<ErrorModel> models = ErrorService.find(reference, action, generator);
                        subscriber.onNext(models);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
        public static Observable<Boolean> contains(final String reference, final ErrorModel.Action action, final ErrorModel.Generator generator) {
            return Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    try {
                        boolean contains = ErrorService.contains(reference, action, generator);
                        subscriber.onNext(contains);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}