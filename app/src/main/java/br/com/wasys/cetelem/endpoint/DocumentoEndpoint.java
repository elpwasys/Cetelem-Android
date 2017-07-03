package br.com.wasys.cetelem.endpoint;

import java.util.List;

import br.com.wasys.cetelem.model.DocumentoModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by pascke on 02/08/16.
 */
public interface DocumentoEndpoint {

    @GET("documento/obter/{id}")
    Call<DocumentoModel> obter(@Path("id") Long id);

    @GET("documento/listar/{id}")
    Call<List<DocumentoModel>> listar(@Path("id") Long id);
}