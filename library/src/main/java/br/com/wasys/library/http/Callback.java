package br.com.wasys.library.http;

import android.util.Log;

import br.com.wasys.library.enumerator.HttpStatus;
import br.com.wasys.library.exception.EndpointException;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by pascke on 03/08/16.
 */
public abstract class Callback<T> implements retrofit2.Callback<T> {

    private static final String TAG = Callback.class.getSimpleName();

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        try {
            Endpoint.evaluate(response);
            int code = response.code();
            T body = response.body();
            HttpStatus status = HttpStatus.valueOf(code);
            onSuccess(body, status);
        } catch (EndpointException e) {
            onError(e.error);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable throwable) {
        Log.e(TAG, "Communication failure!", throwable);
        onFailure(throwable);
    }

    public void onFailure(Throwable throwable) {
        EndpointException exception = Endpoint.create(throwable);
        onError(exception.error);
    }

    public abstract void onError(Error error);
    public abstract void onSuccess(T body, HttpStatus status);
}
