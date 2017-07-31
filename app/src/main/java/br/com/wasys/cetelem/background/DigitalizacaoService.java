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
import br.com.wasys.cetelem.endpoint.DigitalizacaoEndpoint;
import br.com.wasys.cetelem.endpoint.Endpoint;
import br.com.wasys.cetelem.model.ArquivoModel;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.model.TipoDocumentoModel;
import br.com.wasys.cetelem.realm.Arquivo;
import br.com.wasys.cetelem.realm.Digitalizacao;
import br.com.wasys.library.exception.AppException;
import br.com.wasys.library.utils.JacksonUtils;
import br.com.wasys.library.utils.TypeUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by pascke on 28/06/17.
 */

public class DigitalizacaoService extends Service {

    private Looper mLooper;
    private ServiceHandler mHandler;

    private static final String TAG = "Digitalizacao";
    private static final String KEY_TIPO = DigitalizacaoService.class.getName() + ".tipo";
    private static final String KEY_REFERENCIA = DigitalizacaoService.class.getName() + ".reference";

    // HANDLER PARA EXECUCAO DO SERVICO
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                Bundle data = msg.getData();
                String name = data.getString(KEY_TIPO);
                String referencia = data.getString(KEY_REFERENCIA);
                DigitalizacaoModel.Tipo tipo = TypeUtils.parse(DigitalizacaoModel.Tipo.class, name);
                if (tipo != null && StringUtils.isNotBlank(referencia)) {
                    digitalizar(tipo, referencia);
                }
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
    public static void startDigitalizacaoService(Context context, DigitalizacaoModel.Tipo tipo, String referencia) {
        Intent intent = new Intent(context, DigitalizacaoService.class);
        intent.putExtra(DigitalizacaoService.KEY_TIPO, tipo.name());
        intent.putExtra(DigitalizacaoService.KEY_REFERENCIA, referencia);
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
            String tipo = intent.getStringExtra(KEY_TIPO);
            String referencia = intent.getStringExtra(KEY_REFERENCIA);
            // OBTEM A MENSAGEM
            Message message = mHandler.obtainMessage();
            message.arg1 = startId;
            // BUNDLE DE PARAMETROS
            Bundle data = new Bundle();
            data.putString(KEY_TIPO, tipo);
            data.putString(KEY_REFERENCIA, referencia);
            message.setData(data);
            // ENVIA A MENSAGEM PARA SER PROCESSADA
            mHandler.sendMessage(message);
            // REINICIA CASO MORTO
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void digitalizar(DigitalizacaoModel.Tipo tipo, String referencia) {
        Log.d(TAG, "Iniciando digitalizacao " + referencia + "...");
        DigitalizacaoModel model = find(tipo, referencia, DigitalizacaoModel.Status.ERRO, DigitalizacaoModel.Status.AGUARDANDO);
        if (model != null) {
            try {
                update(model.id, DigitalizacaoModel.Status.ENVIANDO);
                upload(tipo, referencia, model.arquivos);
                delete(model.id);
                update(model.id, DigitalizacaoModel.Status.ENVIADO);
                Log.d(TAG, "Sucesso na digitalizacao " + referencia + ".");
            } catch (Throwable throwable) {
                Log.e(TAG, "Erro na digitalizacao " + referencia + ".", throwable);
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
        Log.d(TAG, "Digitalizacao " + referencia + " finalizado.");
    }

    private void delete(Long id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Log.d(TAG, "Listando registros de arquivos da digitalizacao para excluir...");
            realm.beginTransaction();
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
            Log.e(TAG, "Falha na exclusao dos registros da digitalizacao.", e);
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
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
            Log.d(TAG, "Atualizando status da digitalizacao para " + status.name() + "...");
            realm.beginTransaction();
            Digitalizacao digitalizacao = realm.where(Digitalizacao.class)
                    .equalTo("id", id)
                    .findFirst();
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
            Log.d(TAG, "Sucesso na atualizacao da digitalizacao.");
        } catch (Throwable e) {
            Log.e(TAG, "Falha na atualizacao da digitalizacao.", e);
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            throw e;
        } finally {
            realm.close();
        }
    }

    private DigitalizacaoModel find(DigitalizacaoModel.Tipo tipo, String referencia, DigitalizacaoModel.Status... status) {
        String[] names = new String[status.length];
        for (int i = 0; i < status.length; i++) {
            names[i] = status[i].name();
        }
        String join = StringUtils.join(status, ", ");
        Log.d(TAG, "Buscando digitalizacao " + referencia + " status [" + join + "]...");
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Digitalizacao> query = realm.where(Digitalizacao.class)
                    .equalTo("tipo", tipo.name())
                    .equalTo("referencia", referencia);
            for (int i = 0; i < status.length; i++) {
                if (i > 0) {
                    query.or();
                }
                query.equalTo("status", status[i].name());
            }
            Digitalizacao digitalizacao = query.findFirst();
            DigitalizacaoModel digitalizacaoModel = DigitalizacaoModel.from(digitalizacao);
            Log.d(TAG, "Sucesso na busca da digitalizacao " + referencia + " status [" + join + "].");
            return digitalizacaoModel;
        } catch (Throwable e) {
            Log.e(TAG, "Falha na busca da digitalizacao " + referencia + " status [" + join + "].", e);
            throw e;
        } finally {
            realm.close();
        }
    }

    private void upload(DigitalizacaoModel.Tipo tipo, String referencia, List<ArquivoModel> models) throws Throwable {

        Log.d(TAG, "Iniciando o upload dos arquivos da digitalizacao " + referencia + "....");

        if (CollectionUtils.isNotEmpty(models)) {

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

                Log.d(TAG, "Criando Multipart...");

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);

                for (File file : files) {

                    String name = file.getName();
                    String extension = MimeTypeMap.getFileExtensionFromUrl(name);
                    String mimeTypeExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                    MediaType mediaType = MediaType.parse(mimeTypeExtension);
                    RequestBody requestBody = MultipartBody.create(mediaType, file);

                    builder.addFormDataPart("files", name, requestBody);
                }

                Log.d(TAG, "Multipart criado.");

                RequestBody multipartBody = builder.build();


                Long id = Long.valueOf(referencia); // ID DO PROCESSO OU DOCUMENTO
                Long timeout = Long.valueOf(60 * 10); // 10 minutos para conexao, leitura e escrita

                DigitalizacaoEndpoint endpoint = Endpoint.create(DigitalizacaoEndpoint.class, timeout);
                Call<ResultModel> call = endpoint.digitalizar(tipo.name(), id, multipartBody);

                Log.d(TAG, "Enviando imagens...");

                ResultModel resultModel = Endpoint.execute(call);

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
