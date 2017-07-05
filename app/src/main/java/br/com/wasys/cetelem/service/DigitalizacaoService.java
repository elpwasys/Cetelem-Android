package br.com.wasys.cetelem.service;

import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.realm.Digitalizacao;
import br.com.wasys.library.service.Service;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class DigitalizacaoService extends Service {

    public static boolean existsBy(String referencia, DigitalizacaoModel.Tipo tipo, DigitalizacaoModel.Status status) throws Throwable {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Digitalizacao> errors = realm.where(Digitalizacao.class)
                    .equalTo("tipo", tipo.name())
                    .equalTo("status", status.name())
                    .equalTo("referencia", referencia)
                    .findAll();
            return !errors.isEmpty();
        } finally {
            realm.close();
        }
    }

    public static DigitalizacaoModel getBy(String reference, DigitalizacaoModel.Tipo tipo) throws Throwable {
        return getBy(reference, tipo, null);
    }

    public static DigitalizacaoModel getBy(String referencia, DigitalizacaoModel.Tipo tipo, DigitalizacaoModel.Status status) throws Throwable {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Digitalizacao> query = realm.where(Digitalizacao.class)
                    .equalTo("tipo", tipo.name())
                    .equalTo("referencia", referencia);
            if (status != null) {
                query.equalTo("status", status.name());
            }
            Digitalizacao digitalizacao = query.findFirst();
            DigitalizacaoModel model = DigitalizacaoModel.from(digitalizacao);
            return model;
        } finally {
            realm.close();
        }
    }

    public static class Async {

        public static Observable<Boolean> existsBy(final String referencia, final DigitalizacaoModel.Tipo tipo, final DigitalizacaoModel.Status status) {
            return Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    try {
                        boolean exists = DigitalizacaoService.existsBy(referencia, tipo, status);
                        subscriber.onNext(exists);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DigitalizacaoModel> getBy(final String referencia, final DigitalizacaoModel.Tipo tipo) {
            return Observable.create(new Observable.OnSubscribe<DigitalizacaoModel>() {
                @Override
                public void call(Subscriber<? super DigitalizacaoModel> subscriber) {
                    try {
                        DigitalizacaoModel model = DigitalizacaoService.getBy(referencia, tipo);
                        subscriber.onNext(model);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DigitalizacaoModel> getBy(final String referencia, final DigitalizacaoModel.Tipo tipo, final DigitalizacaoModel.Status status) {
            return Observable.create(new Observable.OnSubscribe<DigitalizacaoModel>() {
                @Override
                public void call(Subscriber<? super DigitalizacaoModel> subscriber) {
                    try {
                        DigitalizacaoModel model = DigitalizacaoService.getBy(referencia, tipo, status);
                        subscriber.onNext(model);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}