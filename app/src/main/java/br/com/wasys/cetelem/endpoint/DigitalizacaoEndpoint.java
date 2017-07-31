package br.com.wasys.cetelem.endpoint;

import br.com.wasys.cetelem.model.ResultModel;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by pascke on 02/08/16.
 */
public interface DigitalizacaoEndpoint {

    @POST("digitalizacao/digitalizar/{name}/{id}")
    Call<ResultModel> digitalizar(@Path("name") String name, @Path("id") Long id, @Body RequestBody body);
}