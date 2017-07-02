package br.com.wasys.cetelem.endpoint;

import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.ProcessoMeta;
import br.com.wasys.cetelem.model.PesquisaModel;
import br.com.wasys.cetelem.paging.ProcessoPagingModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by pascke on 02/08/16.
 */
public interface PesquisaEndpoint {

    @GET("pesquisa/dataset")
    Call<DataSet<ProcessoPagingModel, ProcessoMeta>> getDataSet();

    @POST("pesquisa/filtrar")
    Call<ProcessoPagingModel> filtrar(@Body PesquisaModel pesquisaModel);
}
