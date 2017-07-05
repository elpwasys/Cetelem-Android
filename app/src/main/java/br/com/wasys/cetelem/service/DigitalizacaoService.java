package br.com.wasys.cetelem.service;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.realm.Arquivo;
import br.com.wasys.cetelem.realm.Digitalizacao;
import br.com.wasys.library.exception.AppException;
import br.com.wasys.library.service.Service;
import br.com.wasys.library.utils.TypeUtils;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class DigitalizacaoService extends Service {

    public static DigitalizacaoModel criar(String referencia, DigitalizacaoModel.Tipo tipo, List<UploadModel> models) throws Throwable {
        if (CollectionUtils.isNotEmpty(models)) {
            Realm realm = Realm.getDefaultInstance();
            try {
                realm.beginTransaction();
                // REGISTRO DE DIGITALIZACAO
                Digitalizacao digitalizacao = realm.where(Digitalizacao.class)
                        .equalTo("tipo", tipo.name())
                        .equalTo("referencia", referencia)
                        .findFirst();
                Date date = new Date();
                if (digitalizacao != null) {
                    DigitalizacaoModel.Status status = TypeUtils.parse(DigitalizacaoModel.Status.class, digitalizacao.status);
                    if (DigitalizacaoModel.Status.ENVIANDO.equals(status)) {
                        throw new AppException("Aguarde a digitalizacao anterior terminar para poder enviar outra.");
                    }
                } else {
                    Long id = RealmService.getNextId(Digitalizacao.class);
                    digitalizacao = realm.createObject(Digitalizacao.class, id);
                    digitalizacao.tipo = tipo.name();
                    digitalizacao.dataHora = date;
                    digitalizacao.tentativas = 0;
                    digitalizacao.referencia = referencia;
                }
                digitalizacao.status = DigitalizacaoModel.Status.AGUARDANDO.name();
                digitalizacao.mensagem = null;
                digitalizacao.dataHoraEnvio = null;
                digitalizacao.dataHoraRetorno = null;
                // REGISTROS DE ARQUIVO DA DIGITALIZACAO
                for (UploadModel model : models) {
                    String path = model.path;
                    Arquivo arquivo = realm.where(Arquivo.class)
                            .equalTo("caminho", path)
                            .findFirst();
                    if (arquivo == null) {
                        Long id = RealmService.getNextId(Arquivo.class);
                        arquivo = realm.createObject(Arquivo.class, id);
                        arquivo.caminho = path;
                        arquivo.digitalizacao = digitalizacao;
                        digitalizacao.arquivos.add(arquivo);
                    }
                }
                realm.commitTransaction();
                return DigitalizacaoModel.from(digitalizacao);
            } catch (Throwable e) {
                if (realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw e;
            } finally {
                realm.close();
            }
        }
        return null;
    }

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

        public static Observable<DigitalizacaoModel> criar(final String referencia, final DigitalizacaoModel.Tipo tipo, final List<UploadModel> models) {
            return Observable.create(new Observable.OnSubscribe<DigitalizacaoModel>() {
                @Override
                public void call(Subscriber<? super DigitalizacaoModel> subscriber) {
                    try {
                        DigitalizacaoModel model = DigitalizacaoService.criar(referencia, tipo, models);
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