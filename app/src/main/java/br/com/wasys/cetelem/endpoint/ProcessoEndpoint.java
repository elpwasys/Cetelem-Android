package br.com.wasys.cetelem.endpoint;

import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.ProcessoMeta;
import br.com.wasys.cetelem.dataset.meta.TipoProcessoMeta;
import br.com.wasys.cetelem.model.PesquisaModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.TipoProcessoModel;
import br.com.wasys.cetelem.paging.PagingModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by pascke on 02/08/16.
 */
public interface ProcessoEndpoint {

    @GET("processo/dataset")
    Call<DataSet<ProcessoModel, ProcessoMeta>> getDataSet();

    @GET("processo/tipo/dataset/{id}")
    Call<DataSet<TipoProcessoModel, TipoProcessoMeta>> getTipoDataSet(@Path("id") Long id);

    @POST("processo/salvar")
    Call<ProcessoModel> salvar(@Body ProcessoModel processoModel);

    @POST("processo/pesquisar")
    Call<PagingModel<ProcessoModel>> pesquisar(@Body PesquisaModel pesquisaModel);
}
