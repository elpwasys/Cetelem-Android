package br.com.wasys.cetelem.service;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.ProcessoMeta;
import br.com.wasys.cetelem.dataset.meta.TipoProcessoMeta;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.endpoint.ProcessoEndpoint;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.TipoProcessoModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.model.ProcessoRegraModel;
import br.com.wasys.library.service.Service;
import retrofit2.Call;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class ProcessoService extends Service {

    public static ProcessoModel salvar(ProcessoModel processoModel) throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<ProcessoModel> call = endpoint.salvar(processoModel);
        ProcessoModel result = Endpoint.execute(call);
        List<UploadModel> models = processoModel.uploads;
        if (CollectionUtils.isNotEmpty(models)) {
            String referencia = String.valueOf(result.id);
            DigitalizacaoService.criar(referencia, DigitalizacaoModel.Tipo.TIPIFICACAO, models);
        }
        return result;
    }


    public static DataSet<ProcessoModel, ProcessoRegraModel> enviar(Long id) throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<DataSet<ProcessoModel, ProcessoRegraModel>> call = endpoint.enviar(id);
        DataSet<ProcessoModel, ProcessoRegraModel> dataSet = Endpoint.execute(call);
        return dataSet;
    }

    public static DataSet<ProcessoModel, ProcessoRegraModel> editar(Long id) throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<DataSet<ProcessoModel, ProcessoRegraModel>> call = endpoint.editar(id);
        DataSet<ProcessoModel, ProcessoRegraModel> dataSet = Endpoint.execute(call);
        return dataSet;
    }

    public static DataSet<ProcessoModel, ProcessoMeta> getDataSet() throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<DataSet<ProcessoModel, ProcessoMeta>> call = endpoint.getDataSet();
        DataSet<ProcessoModel, ProcessoMeta> dataSet = Endpoint.execute(call);
        return dataSet;
    }

    public static DataSet<TipoProcessoModel, TipoProcessoMeta> getTipoDataSet(Long id) throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<DataSet<TipoProcessoModel, TipoProcessoMeta>> call = endpoint.getTipoDataSet(id);
        DataSet<TipoProcessoModel, TipoProcessoMeta> dataSet = Endpoint.execute(call);
        return dataSet;
    }

    public static class Async {

        public static Observable<ProcessoModel> salvar(final ProcessoModel processoModel) {
            return Observable.create(new Observable.OnSubscribe<ProcessoModel>() {
                @Override
                public void call(Subscriber<? super ProcessoModel> subscriber) {
                    try {
                        ProcessoModel model = ProcessoService.salvar(processoModel);
                        subscriber.onNext(model);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DataSet<ProcessoModel, ProcessoRegraModel>> enviar(final Long id) {
            return Observable.create(new Observable.OnSubscribe<DataSet<ProcessoModel, ProcessoRegraModel>>() {
                @Override
                public void call(Subscriber<? super DataSet<ProcessoModel, ProcessoRegraModel>> subscriber) {
                    try {
                        DataSet<ProcessoModel, ProcessoRegraModel> dataSet = ProcessoService.enviar(id);
                        subscriber.onNext(dataSet);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DataSet<ProcessoModel, ProcessoRegraModel>> editar(final Long id) {
            return Observable.create(new Observable.OnSubscribe<DataSet<ProcessoModel, ProcessoRegraModel>>() {
                @Override
                public void call(Subscriber<? super DataSet<ProcessoModel, ProcessoRegraModel>> subscriber) {
                    try {
                        DataSet<ProcessoModel, ProcessoRegraModel> dataSet = ProcessoService.editar(id);
                        subscriber.onNext(dataSet);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DataSet<ProcessoModel, ProcessoMeta>> getDataSet() {
            return Observable.create(new Observable.OnSubscribe<DataSet<ProcessoModel, ProcessoMeta>>() {
                @Override
                public void call(Subscriber<? super DataSet<ProcessoModel, ProcessoMeta>> subscriber) {
                    try {
                        DataSet<ProcessoModel, ProcessoMeta> dataSet = ProcessoService.getDataSet();
                        subscriber.onNext(dataSet);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<DataSet<TipoProcessoModel, TipoProcessoMeta>> getTipoDataSet(final Long id) {
            return Observable.create(new Observable.OnSubscribe<DataSet<TipoProcessoModel, TipoProcessoMeta>>() {
                @Override
                public void call(Subscriber<? super DataSet<TipoProcessoModel, TipoProcessoMeta>> subscriber) {
                    try {
                        DataSet<TipoProcessoModel, TipoProcessoMeta> dataSet = ProcessoService.getTipoDataSet(id);
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