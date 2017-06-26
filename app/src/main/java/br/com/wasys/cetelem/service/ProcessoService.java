package br.com.wasys.cetelem.service;

import java.util.List;

import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.endpoint.ProcessoEndpoint;
import br.com.wasys.cetelem.model.CampoGrupoModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.tela.Tela;
import br.com.wasys.cetelem.tela.metadata.ProcessoMetadata;
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
        ProcessoModel model = Endpoint.execute(call);
        return model;
    }

    public static List<CampoGrupoModel> listarCampos(Long id) throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<List<CampoGrupoModel>> call = endpoint.listar(id);
        List<CampoGrupoModel> models = Endpoint.execute(call);
        return models;
    }

    public static Tela<ProcessoModel, ProcessoMetadata> cadastrar() throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<Tela<ProcessoModel, ProcessoMetadata>> call = endpoint.cadastrar();
        Tela<ProcessoModel, ProcessoMetadata> tela = Endpoint.execute(call);
        return tela;
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

        public static Observable<List<CampoGrupoModel>> listarCampos(final Long id) {
            return Observable.create(new Observable.OnSubscribe<List<CampoGrupoModel>>() {
                @Override
                public void call(Subscriber<? super List<CampoGrupoModel>> subscriber) {
                    try {
                        List<CampoGrupoModel> models = ProcessoService.listarCampos(id);
                        subscriber.onNext(models);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }

        public static Observable<Tela<ProcessoModel, ProcessoMetadata>> cadastrar() {
            return Observable.create(new Observable.OnSubscribe<Tela<ProcessoModel, ProcessoMetadata>>() {
                @Override
                public void call(Subscriber<? super Tela<ProcessoModel, ProcessoMetadata>> subscriber) {
                    try {
                        Tela<ProcessoModel, ProcessoMetadata> tela = ProcessoService.cadastrar();
                        subscriber.onNext(tela);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}