package br.com.wasys.cetelem.service;

import java.util.ArrayList;

import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.DocumentoMeta;
import br.com.wasys.cetelem.endpoint.DocumentoEndpoint;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.model.JustificativaModel;
import br.com.wasys.cetelem.model.ResultModel;
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

    public static ResultModel justificar(JustificativaModel justificativaModel) throws Throwable {
        DocumentoEndpoint endpoint = Endpoint.create(DocumentoEndpoint.class);
        Call<ResultModel> call = endpoint.justificar(justificativaModel);
        ResultModel model = Endpoint.execute(call);
        return model;
    }


    public static DataSet<ArrayList<DocumentoModel>, DocumentoMeta> enviar(Long id) throws Throwable {
        DocumentoEndpoint endpoint = Endpoint.create(DocumentoEndpoint.class);
        Call<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> call = endpoint.enviar(id);
        DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet = Endpoint.execute(call);
        return dataSet;
    }

    public static DataSet<ArrayList<DocumentoModel>, DocumentoMeta> getDataSet(Long id) throws Throwable {
        DocumentoEndpoint endpoint = Endpoint.create(DocumentoEndpoint.class);
        Call<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> call = endpoint.getDataSet(id);
        DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet = Endpoint.execute(call);
        return dataSet;
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
        public static Observable<ResultModel> justificar(final JustificativaModel justificativaModel) {
            return Observable.create(new Observable.OnSubscribe<ResultModel>() {
                @Override
                public void call(Subscriber<? super ResultModel> subscriber) {
                    try {
                        ResultModel resultModel = DocumentoService.justificar(justificativaModel);
                        subscriber.onNext(resultModel);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
        public static Observable<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> enviar(final Long id) {
            return Observable.create(new Observable.OnSubscribe<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>>() {
                @Override
                public void call(Subscriber<? super DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> subscriber) {
                    try {
                        DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet = DocumentoService.enviar(id);
                        subscriber.onNext(dataSet);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
        public static Observable<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> getDataSet(final Long id) {
            return Observable.create(new Observable.OnSubscribe<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>>() {
                @Override
                public void call(Subscriber<? super DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> subscriber) {
                    try {
                        DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet = DocumentoService.getDataSet(id);
                        subscriber.onNext(dataSet);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}