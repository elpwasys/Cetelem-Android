package br.com.wasys.cetelem.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import com.todobom.opennotescanner.OpenNoteScannerActivity;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.CredencialModel;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends CetelemActivity {

    @BindView(R.id.edit_login) EditText mLoginEditText;
    @BindView(R.id.edit_senha) EditText mSenhaEditText;

    @BindView(R.id.layout_login) TextInputLayout mLoginTextInputLayout;
    @BindView(R.id.layout_senha) TextInputLayout mSenhaTextInputLayout;

    public static Intent create(Context context) {
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
        Intent intent = new Intent(this, OpenNoteScannerActivity.class);
        startActivity(intent);
        /*
        CredencialModel model = validar();
        if (model != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        */
    }

    private CredencialModel validar() {
        boolean valido = true;
        // LOGIN
        String login = FieldUtils.getValue(mLoginEditText);
        if (StringUtils.isBlank(login)) {
            valido = false;
            mLoginTextInputLayout.setError(getString(R.string.msg_required_field, getString(R.string.usuario)));
        } else {
            mLoginTextInputLayout.setErrorEnabled(false);
        }
        // SENHA
        String senha = FieldUtils.getValue(mSenhaEditText);
        if (StringUtils.isBlank(senha)) {
            valido = false;
            mSenhaTextInputLayout.setError(getString(R.string.msg_required_field, getString(R.string.senha)));
        } else {
            mSenhaTextInputLayout.setErrorEnabled(false);
        }
        // MODEL
        CredencialModel model = null;
        if (valido) {
            model = new CredencialModel();
            model.login = login;
            model.senha = senha;
        }
        return model;
    }
}
