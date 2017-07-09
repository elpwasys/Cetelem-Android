package br.com.wasys.cetelem.endpoint;

import java.util.ArrayList;

import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.DocumentoMeta;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.model.JustificativaModel;
import br.com.wasys.cetelem.model.ProcessoLogModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.ResultModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by pascke on 02/08/16.
 */
public interface DocumentoEndpoint {

    @GET("documento/obter/{id}")
    Call<DocumentoModel> obter(@Path("id") Long id);

    @POST("documento/justificar")
    Call<ResultModel> justificar(@Body JustificativaModel justificativaModel);

    @GET("documento/enviar/{id}")
    Call<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> enviar(@Path("id") Long id);

    @GET("documento/dataset/{id}")
    Call<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> getDataSet(@Path("id") Long id);
}