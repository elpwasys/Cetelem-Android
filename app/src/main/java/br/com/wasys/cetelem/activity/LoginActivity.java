package br.com.wasys.cetelem.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

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

    @OnClick(R.id.button_entrar)
    public void onEntrarPressed() {
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
                hideProgress();
                handle(e);
            }
            @Override
            public void onNext(DispositivoModel dispositivoModel) {
                Dispositivo.from(dispositivoModel);
                Intent intent = MainActivity.newIntent(LoginActivity.this);
                startActivity(intent);
                finish();
            }
        });
    }
}