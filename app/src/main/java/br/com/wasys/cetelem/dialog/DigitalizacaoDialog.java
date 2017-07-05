package br.com.wasys.cetelem.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.library.utils.DateUtils;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class DigitalizacaoDialog extends AppCompatDialogFragment {

    @BindView(R.id.text_tipo) TextView mTipoTextView;
    @BindView(R.id.text_data) TextView mDataTextView;
    @BindView(R.id.text_title) TextView mTitleTextView;
    @BindView(R.id.text_mensagem) TextView mMensagemTextView;
    @BindView(R.id.text_reference) TextView mReferenceTextView;
    @BindView(R.id.text_tentativas) TextView mTentativaTextView;
    @BindView(R.id.text_mensagem_section) TextView mMensagemSectionTextView;
    @BindView(R.id.button_reenviar) Button mReenviarButton;

    private DigitalizacaoModel mDigitalizacao;
    private OnUplodErrorListener mOnUplodErrorListener;

    public static DigitalizacaoDialog newInstance(DigitalizacaoModel digitalizacao, OnUplodErrorListener onUplodErrorListener) {
        DigitalizacaoDialog fragment = new DigitalizacaoDialog();
        fragment.mDigitalizacao = digitalizacao;
        fragment.mOnUplodErrorListener = onUplodErrorListener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_digitalizacao, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        ButterKnife.bind(this, view);
        AlertDialog dialog = builder.create();
        populate();
        return dialog;
    }

    private void populate() {
        if (mDigitalizacao != null) {
            String text;
            if (DigitalizacaoModel.Tipo.TIPIFICACAO.equals(mDigitalizacao.tipo)) {
                text = getString(R.string.processo);
            } else {
                text = getString(mDigitalizacao.tipo.stringRes);
            }
            FieldUtils.setText(mTipoTextView, text);
            FieldUtils.setText(mTitleTextView, getString(mDigitalizacao.status.stringRes));
            FieldUtils.setText(mDataTextView, mDigitalizacao.dataHoraRetorno, DateUtils.DateType.DATE_TIME_BR);
            FieldUtils.setText(mReferenceTextView, mDigitalizacao.referencia);
            FieldUtils.setText(mTentativaTextView, mDigitalizacao.tentativas);
            String mensagem = mDigitalizacao.mensagem;
            if (StringUtils.isBlank(mensagem)) {
                mMensagemTextView.setVisibility(View.GONE);
                mMensagemSectionTextView.setVisibility(View.GONE);
            } else {
                mMensagemTextView.setVisibility(View.VISIBLE);
                mMensagemSectionTextView.setVisibility(View.VISIBLE);
                FieldUtils.setText(mMensagemTextView, mensagem);
            }
            DigitalizacaoModel.Status status = mDigitalizacao.status;
            if (!DigitalizacaoModel.Status.ERRO.equals(status) && !DigitalizacaoModel.Status.AGUARDANDO.equals(status)) {
                mReenviarButton.setVisibility(View.INVISIBLE);
            }
            int color;
            switch (status) {
                case ERRO:
                    color = ResourcesCompat.getColor(getResources(), R.color.red_500, null);
                    break;
                case ENVIADO:
                    color = ResourcesCompat.getColor(getResources(), R.color.green_500, null);
                    break;
                default:
                    color = ResourcesCompat.getColor(getResources(), R.color.grey_500, null);
            }
            mTitleTextView.setTextColor(color);
        }
    }

    @OnClick(R.id.button_fechar)
    public void onFecharClick() {
        dismiss();
        if (mOnUplodErrorListener != null) {
            mOnUplodErrorListener.onReenviar(false);
        }
    }

    @OnClick(R.id.button_reenviar)
    public void onReenviarClick() {
        dismiss();
        if (mOnUplodErrorListener != null) {
            mOnUplodErrorListener.onReenviar(true);
        }
    }

    public static interface OnUplodErrorListener {
        void onReenviar(boolean answer);
    }
}
