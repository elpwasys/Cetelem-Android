package br.com.wasys.cetelem.endpoint;

import br.com.wasys.cetelem.model.ResultModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by pascke on 02/08/16.
 */
public interface ImagemEndpoint {

    @GET("{nome}")
    Call<ResponseBody> carregar(@Path("nome") String nome);

    @GET("imagem/excluir/{id}")
    Call<ResultModel> excluir(@Path("id") Long id);
}