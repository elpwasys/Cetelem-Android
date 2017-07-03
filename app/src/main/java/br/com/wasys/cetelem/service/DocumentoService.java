package br.com.wasys.cetelem.service;

import java.util.List;

import br.com.wasys.cetelem.endpoint.DocumentoEndpoint;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.library.service.Service;
import retrofit2.Call;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class DocumentoService extends Service {

    public static DocumentoModel obter(Long id) throws Throwable {
        DocumentoEndpoint endpoint = Endpoint.create(DocumentoEndpoint.class);
        Call<DocumentoModel> call = endpoint.obter(id);
        DocumentoModel model = Endpoint.execute(call);
        return model;
    }

    public static List<DocumentoModel> listar(Long id) throws Throwable {
        DocumentoEndpoint endpoint = Endpoint.create(DocumentoEndpoint.class);
        Call<List<DocumentoModel>> call = endpoint.listar(id);
        List<DocumentoModel> models = Endpoint.execute(call);
        return models;
    }

    public static class Async {
        public static Observable<DocumentoModel> obter(final Long id) {
            return Observable.create(new Observable.OnSubscribe<DocumentoModel>() {
                @Override
                public void call(Subscriber<? super DocumentoModel> subscriber) {
                    try {
                        DocumentoModel model = DocumentoService.obter(id);
                        subscriber.onNext(model);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
        public static Observable<List<DocumentoModel>> listar(final Long id) {
            return Observable.create(new Observable.OnSubscribe<List<DocumentoModel>>() {
                @Override
                public void call(Subscriber<? super List<DocumentoModel>> subscriber) {
                    try {
                        List<DocumentoModel> models = DocumentoService.listar(id);
                        subscriber.onNext(models);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}