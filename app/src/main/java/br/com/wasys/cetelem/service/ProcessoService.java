package br.com.wasys.cetelem.service;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
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
import br.com.wasys.cetelem.realm.Arquivo;
import br.com.wasys.cetelem.realm.Digitalizacao;
import br.com.wasys.library.service.Service;
import io.realm.Realm;
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
            Realm realm = Realm.getDefaultInstance();
            String referencia = String.valueOf(result.id);
            try {
                realm.beginTransaction();
                // REGISTRO DE DIGITALIZACAO
                Digitalizacao digitalizacao = realm.where(Digitalizacao.class)
                        .equalTo("tipo", DigitalizacaoModel.Tipo.TIPIFICACAO.name())
                        .equalTo("referencia", referencia)
                        .findFirst();
                if (digitalizacao == null) {
                    Long id = RealmService.getNextId(Digitalizacao.class);
                    digitalizacao = realm.createObject(Digitalizacao.class, id);
                    digitalizacao.tentativas = 0;
                }
                Date date = new Date();
                digitalizacao.tipo = DigitalizacaoModel.Tipo.TIPIFICACAO.name();
                digitalizacao.status = DigitalizacaoModel.Status.AGUARDANDO.name();
                digitalizacao.mensagem = null;
                digitalizacao.referencia = referencia;
                digitalizacao.dataHora = date;
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
            } catch (Throwable e) {
                if (realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw e;
            } finally {
                realm.close();
            }
        }
        return result;
    }

    public static ProcessoModel editar(Long id) throws Throwable {
        ProcessoEndpoint endpoint = Endpoint.create(ProcessoEndpoint.class);
        Call<ProcessoModel> call = endpoint.editar(id);
        ProcessoModel processoModel = Endpoint.execute(call);
        return processoModel;
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

        public static Observable<ProcessoModel> editar(final Long id) {
            return Observable.create(new Observable.OnSubscribe<ProcessoModel>() {
                @Override
                public void call(Subscriber<? super ProcessoModel> subscriber) {
                    try {
                        ProcessoModel processoModel = ProcessoService.editar(id);
                        subscriber.onNext(processoModel);
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