package br.com.wasys.cetelem.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.model.ErrorModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.realm.Error;
import br.com.wasys.cetelem.realm.Upload;
import br.com.wasys.library.exception.AppException;
import br.com.wasys.library.utils.JacksonUtils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by pascke on 28/06/17.
 */

public class DigitalizacaoService extends Service {

    private Looper mLooper;
    private ServiceHandler mHandler;

    private static final String TAG = "Digitalizacao";
    private static final String KEY_PROCESSO = ProcessoModel.class.getName() + ".id";

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                Bundle data = msg.getData();
                long id = data.getLong(KEY_PROCESSO);
                digitalizar(id);
            } finally {
                stopSelf(msg.arg1);
            }
        }
    }

    public static void start(Context context, Long id) {
        Intent intent = new Intent(context, DigitalizacaoService.class);
        intent.putExtra(DigitalizacaoService.KEY_PROCESSO, id);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mLooper = thread.getLooper();
        mHandler = new ServiceHandler(mLooper);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // INICIA O SERVICO EM SEGUNDO PLANO
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final long id = intent.getLongExtra(KEY_PROCESSO, 0);
            if (id > 0) {
                // OBTEM A MENSAGEM
                Message message = mHandler.obtainMessage();
                message.arg1 = startId;
                // BUNDLE DE PARAMETROS
                Bundle data = new Bundle();
                data.putLong(KEY_PROCESSO, id);
                message.setData(data);
                // ENVIA A MENSAGEM PARA SER PROCESSADA
                mHandler.sendMessage(message);
                // REINICIA CASO MORTO
                return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void digitalizar(Long id) {
        Log.d(TAG, "Iniciando a digitalizacao do processo id " + id + "....");
        List<UploadModel> models = find(id, UploadModel.Status.WAITING);
        if (CollectionUtils.isNotEmpty(models)) {
            update(models, UploadModel.Status.UPLOADING);
            try {
                upload(id, models);
                delete(id);
                Log.d(TAG, "Sucesso na digitalizacao do processo id " + id + ".");
            } catch (Throwable e) {
                Log.e(TAG, "Erro na digitalizacao do processo id " + id + ".", e);
                update(models, UploadModel.Status.ERROR);
                save(id, e);
            }
        }
        Log.d(TAG, "Digitalizacao do processo id " + id + " finalizado.");
    }

    private void save(Long id, Throwable throwable) {
        String message = throwable.getMessage();
        if (!(throwable instanceof AppException)) {
            Throwable rootCause = ExceptionUtils.getRootCause(throwable);
            if (rootCause != null) {
                String rootCauseMessage = rootCause.getMessage();
                if (StringUtils.isNotBlank(rootCauseMessage)) {
                    message = rootCauseMessage;
                }
            }
            if (StringUtils.isBlank(message)) {
                message = getString(R.string.msg_erro_digitalizacao);
            }
        }
        Log.d(TAG, "Iniciando insercao do erro de digitalizacao do processo id " + id + " message '" + message + "'.");
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            Error error = realm.createObject(Error.class);
            error.date = new Date();
            error.message = message;
            error.reference = String.valueOf(id);
            error.action = ErrorModel.Action.UPLOAD.name();
            error.generator = ErrorModel.Generator.PROCESSO.name();
            realm.commitTransaction();
            Log.d(TAG, "Sucesso na insercao do erro do processo id " + id + ".");
        } catch (Throwable e) {
            Log.e(TAG, "Falha na insercao do erro de digitalizacao do processo id " + id + ".", e);
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
        } finally {
            Log.d(TAG, "Insercao do erro do processo id " + id + " finalizado.");
            realm.close();
        }
    }

    private void delete(Long id) {
        Log.d(TAG, "Obtendo registros de arquivos do processo " + id + " enviados para o servidor....");
        Realm realm = Realm.getDefaultInstance();
        String reference = String.valueOf(id);
        RealmResults<Upload> uploads = realm.where(Upload.class)
                .equalTo("reference", reference)
                .equalTo("status", UploadModel.Status.UPLOADING.name())
                .findAll();
        Log.d(TAG, CollectionUtils.size(uploads) + " registros do processo " + id + " para exclusao.");
        if (CollectionUtils.isNotEmpty(uploads)) {
            List<String> paths = new ArrayList<>(uploads.size());
            for (Upload upload : uploads) {
                paths.add(upload.path);
            }
            try {
                realm.beginTransaction();
                uploads.deleteAllFromRealm();
                realm.commitTransaction();
                Log.d(TAG, "Registros de uploads do processo " + id + " excluidos com sucesso.");
                for (String path : paths) {
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                        Log.d(TAG, "Arquivo '"+ path +"' excluido com sucesso.");
                    }
                }
                Log.d(TAG, "Sucesso na exclusao de arquivos do processo id " + id + ".");
            } catch (Throwable e) {
                Log.e(TAG, "Erro na exclusao dos arquivos do processo id " + id + ".", e);
                if (realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw e;
            } finally {
                Log.d(TAG, "Exclusao de arquivos do processo id " + id + " finalizado.");
                realm.close();
            }
        }
    }

    private List<UploadModel> find(Long id, UploadModel.Status status) {
        Log.d(TAG, "Obtendo arquivos de upload do processo " + id + "....");
        Realm realm = Realm.getDefaultInstance();
        try {
            String reference = String.valueOf(id);
            RealmResults<Upload> uploads = realm.where(Upload.class)
                    .equalTo("reference", reference)
                    .equalTo("status", status.name())
                    .findAll();
            List<UploadModel> models = UploadModel.from(uploads);
            Log.d(TAG, CollectionUtils.size(models) + " arquivos do processo " + id + " para upload.");
            return models;
        } finally {
            realm.close();
        }
    }

    private void update(List<UploadModel> models, UploadModel.Status status) {
        if (CollectionUtils.isNotEmpty(models)) {
            Log.d(TAG, "Iniciando o processo de atualizacao do status dos arquivos para " + status.name() + "....");
            Realm realm = Realm.getDefaultInstance();
            try {
                realm.beginTransaction();
                for (UploadModel model : models) {
                    String path = model.path;
                    Upload upload = realm.where(Upload.class)
                            .equalTo("path", path)
                            .findFirst();
                    upload.status = status.name();
                    Log.d(TAG, "Arquivo '" + path + "' para status " + status.name() + ".");
                }
                realm.commitTransaction();
                Log.d(TAG, "Sucesso na atualizacao dos arquivos para " + status.name() + ".");
            } catch (Throwable e) {
                Log.e(TAG, "Erro na atualização dos arquivos para status " + status.name() + ".", e);
                if (realm.isInTransaction()) {
                    realm.cancelTransaction();
                }
                throw e;
            } finally {
                realm.close();
                Log.d(TAG, "Fim do processo de atualizacao do status dos arquivos para " + status.name() + ".");
            }
        }
    }

    private void upload(Long id, List<UploadModel> models) throws Throwable {

        if (CollectionUtils.isNotEmpty(models)) {

            Log.d(TAG, "Iniciando o upload dos arquivos do processo " + id + "....");

            Log.d(TAG, "Listando arquivos para enviar...");
            List<File> files = new ArrayList<>(models.size());
            for (UploadModel model : models) {
                File file = new File(model.path);
                if (file.exists()) {
                    files.add(file);
                    Log.d(TAG, "Arquivo '" + file.getAbsolutePath() + "' encontrado...");
                }
            }

            if (CollectionUtils.isNotEmpty(files)) {

                Log.d(TAG, "Iniciando conexao...");

                Map<String, String> headers = Endpoint.getHeaders();

                String spec = BuildConfig.BASE_URL + "processo/digitalizar";
                URL url = new URL(spec);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Log.d(TAG, "Configurando parametros...");

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

                Log.d(TAG, "Quantidade de imagens para enviar " + files.size() + ".");

                for (File file : files) {

                    String name = file.getName();
                    String extension = MimeTypeMap.getFileExtensionFromUrl(name);

                    Log.d(TAG, "Iniciando stream do arquivo " + name + "...");

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

                    Log.d(TAG, "Stream do arquivo " + name + " finalizado com sucesso.");
                }

                dataOutputStream.flush();
                dataOutputStream.close();

                Log.d(TAG, "Stream de arquivos finalizado com sucesso.");

                Log.d(TAG, "Extraindo resposta do servidor...");

                InputStream inputStream;
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }
                ObjectMapper objectMapper = JacksonUtils.getObjectMapper();
                ResultModel resultModel = objectMapper.readValue(inputStream, ResultModel.class);

                if (!resultModel.success) {
                    Log.e(TAG, "Falha no envio dos arquivos.");
                    String messages = resultModel.getMessages();
                    throw new AppException(messages);
                }
                Log.d(TAG, "Sucesso no envio dos arquivos.");
            }
        }
    }
}
