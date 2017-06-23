package br.com.wasys.cetelem.service;

import br.com.wasys.cetelem.endpoint.DispositivoEndpoint;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.model.CredencialModel;
import br.com.wasys.cetelem.model.DispositivoModel;
import br.com.wasys.library.service.Service;
import retrofit2.Call;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class DispositivoService extends Service {

    public static DispositivoModel autenticar(String login, String senha) throws Throwable {
        DispositivoEndpoint endpoint = Endpoint.create(DispositivoEndpoint.class);
        Call<DispositivoModel> call = endpoint.autenticar(new CredencialModel(login, senha));
        DispositivoModel model = Endpoint.execute(call);
        return model;
    }

    public static class Async {
        public static Observable<DispositivoModel> autenticar(final String login, final String senha) {
            return Observable.create(new Observable.OnSubscribe<DispositivoModel>() {
                @Override
                public void call(Subscriber<? super DispositivoModel> subscriber) {
                    try {
                        DispositivoModel userModel = DispositivoService.autenticar(login, senha);
                        subscriber.onNext(userModel);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}