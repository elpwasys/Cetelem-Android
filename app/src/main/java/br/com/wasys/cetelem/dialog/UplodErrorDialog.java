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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.ErrorModel;
import br.com.wasys.cetelem.model.FiltroModel;
import br.com.wasys.cetelem.service.ErrorService;
import br.com.wasys.library.utils.DateUtils;
import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.TypeUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class UplodErrorDialog extends AppCompatDialogFragment {

    @BindView(R.id.text_data) TextView mDataTextView;
    @BindView(R.id.text_mensagem) TextView mMensagemTextView;
    @BindView(R.id.text_generator) TextView mGeneratorTextView;
    @BindView(R.id.text_reference) TextView mReferenceTextView;

    private ErrorModel mError;
    private String mReference;
    private ErrorModel.Generator mGenerator;
    private OnUplodErrorListener mOnUplodErrorListener;

    private Looper mLooper;
    private HandlerThread mHandlerThread;

    private static final String KEY_REFERENCE = UplodErrorDialog.class.getName() + ".reference";
    private static final String KEY_GENERATOR = UplodErrorDialog.class.getName() + ".generator";

    public static UplodErrorDialog newInstance(Long id, ErrorModel.Generator generator, OnUplodErrorListener onUplodErrorListener) {
        UplodErrorDialog fragment = new UplodErrorDialog();
        fragment.mGenerator = generator;
        fragment.mReference = String.valueOf(id);
        fragment.mOnUplodErrorListener = onUplodErrorListener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_uplod_error, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        ButterKnife.bind(this, view);
        AlertDialog dialog = builder.create();
        iniciar();
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

    private void popular(ErrorModel model) {
        mError = model;
        if (mError != null) {
            FieldUtils.setText(mDataTextView, mError.date, DateUtils.DateType.DATE_TIME_BR);
            FieldUtils.setText(mMensagemTextView, mError.message);
            FieldUtils.setText(mReferenceTextView, mError.reference);
            FieldUtils.setText(mGeneratorTextView, getString(mError.generator.stringRes));
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

    private void iniciar() {
        Bundle bundle = getArguments();
        if (StringUtils.isNotBlank(mReference) && mGenerator != null) {
            Observable<List<ErrorModel>> observable = ErrorService.Async.find(mReference, ErrorModel.Action.UPLOAD, mGenerator);
            observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.from(getLooper()))
                    .subscribe(new Subscriber<List<ErrorModel>>() {
                        public void onCompleted() {

                        }
                        @Override
                        public void onError(Throwable e) {

                        }
                        @Override
                        public void onNext(List<ErrorModel> errorList) {
                            int size = CollectionUtils.size(errorList);
                            if (size > 0) {
                                int index = size - 1;
                                ErrorModel errorModel = errorList.get(index);
                                popular(errorModel);
                            }
                        }
                    });
        }
    }

    public static interface OnUplodErrorListener {
        void onReenviar(boolean answer);
    }
}
