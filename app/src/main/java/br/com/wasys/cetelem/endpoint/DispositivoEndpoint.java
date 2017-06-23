package br.com.wasys.cetelem.endpoint;

import br.com.wasys.cetelem.model.CredencialModel;
import br.com.wasys.cetelem.model.DispositivoModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by pascke on 02/08/16.
 */
public interface DispositivoEndpoint {

    @POST("dispositivo/autenticar")
    Call<DispositivoModel> autenticar(@Body CredencialModel credencialModel);
}
