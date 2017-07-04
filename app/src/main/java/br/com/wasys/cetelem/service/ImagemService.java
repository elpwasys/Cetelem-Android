package br.com.wasys.cetelem.service;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import br.com.wasys.cetelem.Application;
import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.endpoint.ImagemEndpoint;
import br.com.wasys.cetelem.model.ImagemModel;
import br.com.wasys.library.http.Endpoint;
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

    private static final String TAG = "I";

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
            Map<String, String> headers = br.com.wasys.cetelem.endpoint.Endpoint.getHeaders();
            String baseURL = BuildConfig.SERVER_URL + dirs + "/";
            ImagemEndpoint endpoint = Endpoint.create(ImagemEndpoint.class, baseURL, headers);
            Call<ResponseBody> call = endpoint.carregar(fileName);
            ResponseBody responseBody = Endpoint.execute(call);
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

    public static class Async {
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