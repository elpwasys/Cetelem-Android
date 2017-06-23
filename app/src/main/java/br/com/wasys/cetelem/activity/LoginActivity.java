package br.com.wasys.cetelem.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import br.com.wasys.cetelem.Dispositivo;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.DispositivoModel;
import br.com.wasys.cetelem.service.DispositivoService;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;

public class LoginActivity extends CetelemActivity {

    @BindView(R.id.edit_login) EditText mLoginEditText;
    @BindView(R.id.edit_senha) EditText mSenhaEditText;

    @BindView(R.id.layout_login) TextInputLayout mLoginTextInputLayout;
    @BindView(R.id.layout_senha) TextInputLayout mSenhaTextInputLayout;

    private Uri mUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        AndroidUtils.requestFocus(mLoginEditText, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {

                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            File file = new File(mUri.getPath());
            if (file.exists()) {
                file.delete();
            }
            mUri = null;
        }
    }

    @OnClick(R.id.button_entrar)
    public void onEntrarPressed() {
        /*
        if (AndroidUtils.isExternalStorageWritable()) {
            String applicationId = BuildConfig.APPLICATION_ID;
            File storage = new File(Environment.getExternalStorageDirectory(), applicationId);
            if (!storage.exists()) {
                storage.mkdirs();
            }
            Date date = new Date();
            String name = DateUtils.format(date, "yyyyMMdd_HHmmss'.jpg'");
            String path = storage.getAbsolutePath() + File.separator + name;
            File file = new File(path);
            try {
                file.createNewFile();
                if (file != null) {
                    mUri = Uri.fromFile(file);
                    Intent intent = OpenNoteScannerActivity.newIntent(this, mUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
        boolean valid = isValid();
        if (valid) {
            autenticar();
        }
    }

    private boolean isValid() {
        boolean valid = true;
        // LOGIN
        String login = FieldUtils.getValue(mLoginEditText);
        if (StringUtils.isBlank(login)) {
            valid = false;
            mLoginTextInputLayout.setError(getString(R.string.msg_required_field, getString(R.string.usuario)));
        } else {
            mLoginTextInputLayout.setErrorEnabled(false);
        }
        // SENHA
        String senha = FieldUtils.getValue(mSenhaEditText);
        if (StringUtils.isBlank(senha)) {
            valid = false;
            mSenhaTextInputLayout.setError(getString(R.string.msg_required_field, getString(R.string.senha)));
        } else {
            mSenhaTextInputLayout.setErrorEnabled(false);
        }
        return valid;
    }

    private void autenticar() {
        String login = FieldUtils.getValue(mLoginEditText);
        String senha = FieldUtils.getValue(mSenhaEditText);
        showProgress();
        Observable<DispositivoModel> observable = DispositivoService.Async.autenticar(login, senha);
        prepare(observable).subscribe(new Subscriber<DispositivoModel>() {
            @Override
            public void onCompleted() {
                hideProgress();
            }
            @Override
            public void onError(Throwable e) {

            }
            @Override
            public void onNext(DispositivoModel dispositivoModel) {
                new Dispositivo(dispositivoModel.id, dispositivoModel.token);
            }
        });
    }
}
