package br.com.wasys.cetelem.service;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import br.com.wasys.cetelem.Application;
import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.endpoint.ImagemEndpoint;
import br.com.wasys.cetelem.model.ImagemModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.library.service.Service;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.FileUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 03/09/16.
 */
public class ImagemService extends Service {

    private static final String TAG = "Imagem";

    public static Uri getViewerUri(String path) {
        Uri uri = null;
        File origin = new File(path);
        if (origin.exists()) {
            if (AndroidUtils.isExternalStorageWritable()) {
                String applicationId = BuildConfig.APPLICATION_ID;
                File storage = new File(Environment.getExternalStorageDirectory(), applicationId);
                if (!storage.exists()) {
                    storage.mkdirs();
                }
                try {
                    String absolutePath = storage.getAbsolutePath() + File.separator + "viewer.jpg";
                    File destination = new File(absolutePath);
                    destination.createNewFile();
                    FileUtils.copy(origin, destination);
                    uri = Uri.fromFile(destination);
                } catch (IOException e) {
                    Log.e("Viewer", "Fail to create image viewer.", e);
                }
            }
        }
        return uri;
    }

    public static ImagemModel carregar(String caminho) throws Throwable {
        ImagemModel model = new ImagemModel();
        String dirs = caminho.substring(0, caminho.lastIndexOf("/"));
        String dirName = Application.getContext().getCacheDir().getAbsolutePath() + File.separator + dirs;
        String fileName = caminho.substring(caminho.lastIndexOf("/") + 1);
        File file = new File(dirName, fileName);
        model.path = file.getAbsolutePath();
        model.cache = true;
        if (!file.exists()) {
            Map<String, String> headers = Endpoint.getHeaders();
            String baseURL = BuildConfig.URL_BASE + "/" + dirs + "/";
            ImagemEndpoint endpoint = br.com.wasys.library.http.Endpoint.create(ImagemEndpoint.class, baseURL, headers);
            Call<ResponseBody> call = endpoint.carregar(fileName);
            ResponseBody responseBody = br.com.wasys.library.http.Endpoint.execute(call);
            byte[] bytes = responseBody.bytes();
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        return model;
    }

    public static ResultModel excluir(Long id) throws Throwable {
        ImagemEndpoint endpoint = Endpoint.create(ImagemEndpoint.class);
        Call<ResultModel> call = endpoint.excluir(id);
        ResultModel model = Endpoint.execute(call);
        return model;
    }

    public static class Async {
        public static Observable<ResultModel> excluir(final Long id) {
            return Observable.create(new Observable.OnSubscribe<ResultModel>() {
                @Override
                public void call(Subscriber<? super ResultModel> subscriber) {
                    try {
                        ResultModel model = ImagemService.excluir(id);
                        subscriber.onNext(model);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
        public static Observable<ImagemModel> carregar(final String caminho) {
            return Observable.create(new Observable.OnSubscribe<ImagemModel>() {
                @Override
                public void call(Subscriber<? super ImagemModel> subscriber) {
                    try {
                        ImagemModel model = ImagemService.carregar(caminho);
                        subscriber.onNext(model);
                        subscriber.onCompleted();
                    } catch (Throwable e) {
                        subscriber.onError(e);
                    }
                }
            });
        }
    }
}