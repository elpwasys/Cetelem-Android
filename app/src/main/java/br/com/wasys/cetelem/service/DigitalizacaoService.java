package br.com.wasys.cetelem.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.realm.Upload;
import br.com.wasys.library.utils.JacksonUtils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by pascke on 28/06/17.
 */

public class DigitalizacaoService extends Service {

    public static final String KEY_PROCESSO = ProcessoModel.class.getName() + ".id";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final long id = intent.getLongExtra(KEY_PROCESSO, 0);
            if (id > 0) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            digitalizar(id);
                        } finally {
                            stopSelf();
                        }
                    }
                });
                thread.start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void digitalizar(Long id) {
        List<UploadModel> models = find(id, UploadModel.Status.WAITING);
        if (CollectionUtils.isNotEmpty(models)) {
            update(models, UploadModel.Status.UPLOADING);
            try {
                upload(id, models);
                delete(id);
            } catch (Throwable e) {
                update(models, UploadModel.Status.ERROR);
            }
        }
    }

    private void delete(Long id) {
        Realm realm = Realm.getDefaultInstance();
        String reference = String.valueOf(id);
        RealmResults<Upload> uploads = realm.where(Upload.class)
                .equalTo("reference", reference)
                .equalTo("status", UploadModel.Status.UPLOADING.name())
                .findAll();
        if (CollectionUtils.isNotEmpty(uploads)) {
            List<String> paths = new ArrayList<>(uploads.size());
            for (Upload upload : uploads) {
                paths.add(upload.path);
            }
            try {
                realm.beginTransaction();
                uploads.deleteAllFromRealm();
                realm.commitTransaction();
                for (String path : paths) {
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Throwable e) {
                if (realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw e;
            } finally {
                realm.close();
            }
        }
    }

    private List<UploadModel> find(Long id, UploadModel.Status status) {
        Realm realm = Realm.getDefaultInstance();
        String reference = String.valueOf(id);
        RealmResults<Upload> uploads = realm.where(Upload.class)
                .equalTo("reference", reference)
                .equalTo("status", status.name())
                .findAll();
        List<UploadModel> models = UploadModel.from(uploads);
        return models;
    }

    private void update(List<UploadModel> models, UploadModel.Status status) {
        if (CollectionUtils.isNotEmpty(models)) {
            Realm realm = Realm.getDefaultInstance();
            try {
                realm.beginTransaction();
                for (UploadModel model : models) {
                    String path = model.path;
                    Upload upload = realm.where(Upload.class)
                            .equalTo("path", path)
                            .findFirst();
                    upload.status = status.name();
                }
                realm.commitTransaction();
            } catch (Throwable e) {
                if (realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw e;
            } finally {
                realm.close();
            }
        }
    }

    private void upload(Long id, List<UploadModel> models) throws Throwable {

        if (CollectionUtils.isNotEmpty(models)) {

            List<File> files = new ArrayList<>(models.size());
            for (UploadModel model : models) {
                File file = new File(model.path);
                if (file.exists()) {
                    files.add(file);
                }
            }

            if (CollectionUtils.isNotEmpty(files)) {

                Map<String, String> headers = Endpoint.getHeaders();

                String spec = BuildConfig.BASE_URL + "processo/digitalizar";
                URL url = new URL(spec);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                if (MapUtils.isNotEmpty(headers)) {
                    Set<Map.Entry<String, String>> entries = headers.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        connection.setRequestProperty(key, value);
                    }
                }

                connection.connect();

                OutputStream outputStream = connection.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                dataOutputStream.writeLong(id); // ID DO PROCESSO
                dataOutputStream.writeInt(files.size()); // QUANTIDADE DE IMAGENS

                for (File file : files) {

                    String name = file.getName();
                    String extension = MimeTypeMap.getFileExtensionFromUrl(name);

                    dataOutputStream.writeUTF(extension); // EXTENSAO DO ARQUIVO
                    dataOutputStream.writeLong(file.length()); // TAMANHO DO ARQUIVO

                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                    // BYTES DO ARQUIVO
                    int i = 0;
                    byte[] bytes = new byte[1024];
                    while ((i = bufferedInputStream.read(bytes)) != -1) {
                        dataOutputStream.write(bytes, 0, i);
                        dataOutputStream.flush();
                    }
                }

                dataOutputStream.flush();
                dataOutputStream.close();

                InputStream inputStream = connection.getInputStream();
                ObjectMapper objectMapper = JacksonUtils.getObjectMapper();

                ResultModel resultModel = objectMapper.readValue(inputStream, ResultModel.class);
                if (!resultModel.success) {
                    throw new Exception("Failed to send images");
                }
            }
        }
    }
}
