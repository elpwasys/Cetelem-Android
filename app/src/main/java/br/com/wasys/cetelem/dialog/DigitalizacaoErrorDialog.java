package br.com.wasys.cetelem.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.service.DigitalizacaoService;
import br.com.wasys.library.utils.DateUtils;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class DigitalizacaoErrorDialog extends AppCompatDialogFragment {

    @BindView(R.id.text_data) TextView mDataTextView;
    @BindView(R.id.text_mensagem) TextView mMensagemTextView;
    @BindView(R.id.text_generator) TextView mGeneratorTextView;
    @BindView(R.id.text_reference) TextView mReferenceTextView;

    private String mReference;
    private DigitalizacaoModel.Tipo mTipo;
    private DigitalizacaoModel mDigitalizacao;
    private OnUplodErrorListener mOnUplodErrorListener;

    private Looper mLooper;
    private HandlerThread mHandlerThread;

    private static final String KEY_REFERENCE = DigitalizacaoErrorDialog.class.getName() + ".reference";
    private static final String KEY_GENERATOR = DigitalizacaoErrorDialog.class.getName() + ".generator";

    public static DigitalizacaoErrorDialog newInstance(Long id, DigitalizacaoModel.Tipo tipo, OnUplodErrorListener onUplodErrorListener) {
        DigitalizacaoErrorDialog fragment = new DigitalizacaoErrorDialog();
        fragment.mTipo = tipo;
        fragment.mReference = String.valueOf(id);
        fragment.mOnUplodErrorListener = onUplodErrorListener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_digitalizacao_error, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        ButterKnife.bind(this, view);
        AlertDialog dialog = builder.create();
        startAsyncDigitalizacao();
        return dialog;
    }

    private Looper getLooper() {
        if (mLooper == null) {
            if (mHandlerThread == null) {
                String name = getClass().getSimpleName();
                mHandlerThread = new HandlerThread(name, Process.THREAD_PRIORITY_BACKGROUND);
                mHandlerThread.start();
            }
            mLooper = mHandlerThread.getLooper();
        }
        return mLooper;
    }

    private void onAsyncDigitalizacao(DigitalizacaoModel model) {
        mDigitalizacao = model;
        if (mDigitalizacao != null) {
            FieldUtils.setText(mDataTextView, mDigitalizacao.dataHoraRetorno, DateUtils.DateType.DATE_TIME_BR);
            FieldUtils.setText(mMensagemTextView, mDigitalizacao.mensagem);
            FieldUtils.setText(mReferenceTextView, mDigitalizacao.referencia);
            FieldUtils.setText(mGeneratorTextView, getString(mDigitalizacao.tipo.stringRes));
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

    private void startAsyncDigitalizacao() {

        if (StringUtils.isNotBlank(mReference) && mTipo != null) {
            Observable<DigitalizacaoModel> observable = DigitalizacaoService.Async.getBy(mReference, mTipo, DigitalizacaoModel.Status.ERRO);
            observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.from(getLooper()))
                    .subscribe(new Subscriber<DigitalizacaoModel>() {
                        public void onCompleted() {

                        }
                        @Override
                        public void onError(Throwable e) {

                        }
                        @Override
                        public void onNext(DigitalizacaoModel model) {
                            onAsyncDigitalizacao(model);
                        }
                    });
        }
    }

    public static interface OnUplodErrorListener {
        void onReenviar(boolean answer);
    }
}
