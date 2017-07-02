package br.com.wasys.cetelem.service;

import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.ProcessoMeta;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.endpoint.PesquisaEndpoint;
import br.com.wasys.cetelem.endpoint.ProcessoEndpoint;
import br.com.wasys.cetelem.model.PesquisaModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.paging.ProcessoPagingModel;
import br.com.wasys.library.service.Service;
import retrofit2.Call;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class PesquisaService extends Service {

    public static ProcessoPagingModel filtrar(PesquisaModel pesquisaModel) throws Throwable {
        PesquisaEndpoint endpoint = Endpoint.create(PesquisaEndpoint.class);
        Call<ProcessoPagingModel> call = endpoint.filtrar(pesquisaModel);
        ProcessoPagingModel pagingModel = Endpoint.execute(call);
        return pagingModel;
    }

    public static DataSet<ProcessoPagingModel, ProcessoMeta> getDataSet() throws Throwable {
        PesquisaEndpoint endpoint = Endpoint.create(PesquisaEndpoint.class);
        Call<DataSet<ProcessoPagingModel, ProcessoMeta>> call = endpoint.getDataSet();
        DataSet<ProcessoPagingModel, ProcessoMeta> dataSet = Endpoint.execute(call);
        return dataSet;
    }

    public static class Async {

        public static Observable<ProcessoPagingModel> filtrar(final PesquisaModel pesquisaModel) {
            return Observable.create(new Observable.OnSubscribe<ProcessoPagingModel>() {
                @Override
                public void call(Subscriber<? super ProcessoPagingModel> subscriber) {
                    try {
                        ProcessoPagingModel pagingModel = PesquisaService.filtrar(pesquisaModel);
                        subscriber.onNext(pagingModel);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DataSet<ProcessoPagingModel, ProcessoMeta>> getDataSet() {
            return Observable.create(new Observable.OnSubscribe<DataSet<ProcessoPagingModel, ProcessoMeta>>() {
                @Override
                public void call(Subscriber<? super DataSet<ProcessoPagingModel, ProcessoMeta>> subscriber) {
                    try {
                        DataSet<ProcessoPagingModel, ProcessoMeta> dataSet = PesquisaService.getDataSet();
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