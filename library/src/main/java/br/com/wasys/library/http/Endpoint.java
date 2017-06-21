package br.com.wasys.library.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;

import br.com.wasys.library.Application;
import br.com.wasys.library.R;
import br.com.wasys.library.enumerator.HttpStatus;
import br.com.wasys.library.enumerator.MediaType;
import br.com.wasys.library.exception.EndpointException;
import br.com.wasys.library.utils.JacksonUtils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by pascke on 03/08/16.
 */
public class Endpoint {

    private static final String TAG = Endpoint.class.getSimpleName();

    public static <T> T create(Class<T> clazz, String baseUrl, final Map<String, String> headers) {
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request()
                        .newBuilder()
                        .addHeader("Content-Type", MediaType.APPLICATION_JSON.value);
                if (MapUtils.isNotEmpty(headers)) {
                    Set<Map.Entry<String, String>> entries = headers.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        builder.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                Request request = builder.build();
                return chain.proceed(request);
            }
        };
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder
                //.readTimeout(30, TimeUnit.SECONDS)
                //.writeTimeout(30, TimeUnit.SECONDS)
                //.connectTimeout(30, TimeUnit.SECONDS)
                .build();
        ObjectMapper objectMapper = JacksonUtils.getObjectMapper();
        JacksonConverterFactory converterFactory = JacksonConverterFactory.create(objectMapper);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(converterFactory)
                .client(client)
                .build();
        T endpoint = retrofit.create(clazz);
        return endpoint;
    }

    public static <T> T execute(Call<T> call) throws EndpointException {
        try {
            Response<T> response = call.execute();
            evaluate(response);
            return response.body();
        } catch (IOException e) {
            throw create(e);
        }
    }

    public static EndpointException create(Throwable throwable) {
        Error error = new Error();
        Application application = Application.getInstance();
        if (throwable instanceof ConnectException) {
            String message = application.getString(R.string.http_error_io);
            error.addMessage(message);
        } else if (throwable instanceof SocketTimeoutException) {
            error.status = HttpStatus.REQUEST_TIMEOUT;
            String message = application.getString(R.string.http_error_timeout);
            error.addMessage(message);
        } else {
            error.status = HttpStatus.UNKNOWN;
            String message = throwable.getMessage();
            Throwable rootCause = ExceptionUtils.getRootCause(throwable);
            if (rootCause != null) {
                message = rootCause.getMessage();
            }
            if (StringUtils.isEmpty(message)) {
                message = error.status.reason;
            }
            error.addMessage(message);
        }
        return new EndpointException(error);
    }

    public static void evaluate(Response response) throws EndpointException {
        int code = response.code();
        HttpStatus status = HttpStatus.valueOf(code);
        if (!status.is2xxSuccessful()) {
            Error error = null;
            ResponseBody errorBody = response.errorBody();
            try {
                String responseText = errorBody.string();
                if (StringUtils.isNotBlank(responseText)) {
                    ObjectMapper objectMapper = JacksonUtils.getObjectMapper();
                    error = objectMapper.readValue(responseText, Error.class);
                }
            } catch (JsonParseException e) {
                error = new Error(HttpStatus.NOT_APPLY);
                error.addMessage("Error, responseText does not conform to JSON syntax as per specification!");
            } catch (JsonMappingException e) {
                error = new Error(HttpStatus.NOT_APPLY);
                error.addMessage("Error, responseText JSON structure does not match structure expected for result type!");
            } catch (IOException e) {
                error = new Error(HttpStatus.UNKNOWN);
                error.addMessage("Error getting responseText!");
            }
            if (error == null) {
                error = new Error();
                error.status = HttpStatus.UNKNOWN;
                error.addMessage(error.status.reason);
            }
            throw new EndpointException(error);
        }
    }
}