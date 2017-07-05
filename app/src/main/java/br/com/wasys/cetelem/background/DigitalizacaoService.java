package br.com.wasys.cetelem.background;

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
import br.com.wasys.cetelem.model.ArquivoModel;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.realm.Arquivo;
import br.com.wasys.cetelem.realm.Digitalizacao;
import br.com.wasys.library.exception.AppException;
import br.com.wasys.library.utils.JacksonUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;

/**
 * Created by pascke on 28/06/17.
 */

public class DigitalizacaoService extends Service {

    private Looper mLooper;
    private ServiceHandler mHandler;

    private static final String TAG = "Digitalizacao";
    private static final String KEY_REFERENCE = DigitalizacaoService.class.getName() + ".id";

    // HANDLER PARA EXECUCAO DO SERVICO
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                Bundle data = msg.getData();
                long id = data.getLong(KEY_REFERENCE);
                digitalizar(id);
            } finally {
                stopSelf(msg.arg1);
            }
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mLooper = thread.getLooper();
        mHandler = new ServiceHandler(mLooper);
    }

    // METODO PARA INICIAR O PROCESSO
    public static void start(Context context, Long id) {
        Intent intent = new Intent(context, DigitalizacaoService.class);
        intent.putExtra(DigitalizacaoService.KEY_REFERENCE, id);
        context.startService(intent);
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
            final long id = intent.getLongExtra(KEY_REFERENCE, 0);
            if (id > 0) {
                // OBTEM A MENSAGEM
                Message message = mHandler.obtainMessage();
                message.arg1 = startId;
                // BUNDLE DE PARAMETROS
                Bundle data = new Bundle();
                data.putLong(KEY_REFERENCE, id);
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
        Log.d(TAG, "Iniciando a digitalizacao do processo id " + id + "...");
        String referencia = String.valueOf(id);
        DigitalizacaoModel model = find(referencia, DigitalizacaoModel.Status.ERRO, DigitalizacaoModel.Status.AGUARDANDO);
        if (model != null) {
            try {
                update(model.id, DigitalizacaoModel.Status.ENVIANDO);
                upload(id, model.arquivos);
                delete(model.id);
                update(model.id, DigitalizacaoModel.Status.ENVIADO);
                Log.d(TAG, "Sucesso na digitalizacao do processo id " + id + ".");
            } catch (Throwable throwable) {
                Log.e(TAG, "Erro na digitalizacao do processo id " + id + ".", throwable);
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
                update(model.id, DigitalizacaoModel.Status.ERRO, message);
            }
        }
        Log.d(TAG, "Digitalizacao do processo id " + id + " finalizado.");
    }

    private void delete(Long id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Log.d(TAG, "Listando registros de arquivos da digitalizacao id " + id + " para excluir...");
            Digitalizacao digitalizacao = realm.where(Digitalizacao.class)
                    .equalTo("id", id)
                    .findFirst();
            RealmList<Arquivo> arquivos = digitalizacao.arquivos;
            if (CollectionUtils.isNotEmpty(arquivos)) {
                List<String> caminhos = new ArrayList<>(arquivos.size());
                for (Arquivo arquivo : arquivos) {
                    caminhos.add(arquivo.caminho);
                }
                Log.d(TAG, "Excluindo registros de arquivos...");
                realm.beginTransaction();
                arquivos.deleteAllFromRealm();
                realm.commitTransaction();
                Log.d(TAG, "Sucesso na exclusao dos registros de arquivos.");
                Log.d(TAG, "Iniciando exclusao dos arquivos fisicos");
                for (String caminho : caminhos) {
                    File file = new File(caminho);
                    if (file.exists()) {
                        file.delete();
                        Log.d(TAG, "Arquivo '"+ caminho +"' excluido com sucesso.");
                    }
                }
                Log.d(TAG, "Sucesso na exclusao dos arquivos fisicos.");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Falha na exclusao dos registros da digitalizacao id " + id + ".", e);
            throw e;
        } finally {
            realm.close();
        }
    }

    private void update(Long id, DigitalizacaoModel.Status status) {
        update(id, status, null);
    }

    private void update(Long id, DigitalizacaoModel.Status status, String mensagem) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Log.d(TAG, "Atualizando status da digitalizacao id " + id + " para " + status.name() + "...");
            Digitalizacao digitalizacao = realm.where(Digitalizacao.class)
                    .equalTo("id", id)
                    .findFirst();
            realm.beginTransaction();
            digitalizacao.status = status.name();
            digitalizacao.mensagem = mensagem;
            Date data = new Date();
            if (DigitalizacaoModel.Status.ENVIANDO.equals(status)) {
                Integer tentativas = digitalizacao.tentativas;
                digitalizacao.tentativas = tentativas + 1;
                digitalizacao.dataHoraEnvio = data;
                Log.d(TAG, "Tentativa da digitalizacao incrementada de " + tentativas + " para " + digitalizacao.tentativas + ".");
            } else if (DigitalizacaoModel.Status.ERRO.equals(status) || DigitalizacaoModel.Status.ENVIADO.equals(status)) {
                digitalizacao.dataHoraRetorno = data;
            }
            realm.commitTransaction();
            Log.d(TAG, "Sucesso na atualizacao da digitalizacao id " + id + ".");
        } catch (Throwable e) {
            Log.e(TAG, "Falha na atualizacao da digitalizacao id " + id + ".", e);
            throw e;
        } finally {
            realm.close();
        }
    }

    private DigitalizacaoModel find(String referencia, DigitalizacaoModel.Status... status) {
        String[] names = new String[status.length];
        for (int i = 0; i < status.length; i++) {
            names[i] = status[i].name();
        }
        String join = StringUtils.join(status, ", ");
        Log.d(TAG, "Buscando digitalizacao referencia " + referencia + " status [" + join + "]...");
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Digitalizacao> query = realm.where(Digitalizacao.class)
                    .equalTo("referencia", referencia)
                    .equalTo("tipo", DigitalizacaoModel.Tipo.TIPIFICACAO.name());
            for (int i = 0; i < status.length; i++) {
                if (i > 0) {
                    query.or();
                }
                query.equalTo("status", status[i].name());
            }
            Digitalizacao digitalizacao = query.findFirst();
            DigitalizacaoModel digitalizacaoModel = DigitalizacaoModel.from(digitalizacao);
            Log.d(TAG, "Sucesso na busca da digitalizacao referencia " + referencia + " status [" + join + "].");
            return digitalizacaoModel;
        } catch (Throwable e) {
            Log.e(TAG, "Falha na busca da digitalizacao referencia " + referencia + " status [" + join + "].", e);
            throw e;
        } finally {
            realm.close();
        }
    }

    private void upload(Long id, List<ArquivoModel> models) throws Throwable {

        if (CollectionUtils.isNotEmpty(models)) {

            Log.d(TAG, "Iniciando o upload das imagens do processo " + id + "....");

            Log.d(TAG, "Listando imagens para enviar...");
            List<File> files = new ArrayList<>(models.size());
            for (ArquivoModel model : models) {
                File file = new File(model.caminho);
                if (file.exists()) {
                    files.add(file);
                    Log.d(TAG, "Imagem '" + file.getAbsolutePath() + "' encontrado...");
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

                    Log.d(TAG, "Iniciando stream da imagem " + name + "...");

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

                    Log.d(TAG, "Stream da imagem " + name + " finalizado com sucesso.");
                }

                dataOutputStream.flush();
                dataOutputStream.close();

                Log.d(TAG, "Stream das imagens finalizado com sucesso.");

                Log.d(TAG, "Extraindo resposta do servidor...");

                InputStream inputStream;
                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Status da resposta " + responseCode + ".");

                if (responseCode == 200) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                ObjectMapper objectMapper = JacksonUtils.getObjectMapper();
                ResultModel resultModel = objectMapper.readValue(inputStream, ResultModel.class);

                if (!resultModel.success) {
                    Log.e(TAG, "Falha no envio das imagens.");
                    String messages = resultModel.getMessages();
                    throw new AppException(messages);
                }

                Log.d(TAG, "Sucesso no envio das imagens.");
            }
        }
    }
}
