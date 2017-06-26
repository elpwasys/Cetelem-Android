package br.com.wasys.cetelem.endpoint;

import java.util.List;

import br.com.wasys.cetelem.model.CampoGrupoModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.tela.Tela;
import br.com.wasys.cetelem.tela.metadata.ProcessoMetadata;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by pascke on 02/08/16.
 */
public interface ProcessoEndpoint {

    @GET("processo/cadastrar")
    Call<Tela<ProcessoModel, ProcessoMetadata>> cadastrar();

    @GET("processo/campo/listar/{id}")
    Call<List<CampoGrupoModel>> listar(@Path("id") Long id);

    @POST("processo/salvar")
    Call<ProcessoModel> salvar(@Body ProcessoModel processoModel);
}
