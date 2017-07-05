package br.com.wasys.cetelem.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class PendenciaDialog extends AppCompatDialogFragment {

    @BindView(R.id.text_title) TextView mTitleTextView;
    @BindView(R.id.text_observacao) TextView mObservacaoTextView;
    @BindView(R.id.edit_jutificativa) EditText mJustificativaEditText;
    @BindView(R.id.layout_justificativa) TextInputLayout mJustificativaTextInputLayout;

    private DocumentoModel mDocumento;
    private OnPendenciaDialogListener mOnPendenciaDialogListener;

    public static PendenciaDialog newInstance(DocumentoModel documento, OnPendenciaDialogListener onPendenciaDialogListener) {
        PendenciaDialog fragment = new PendenciaDialog();
        fragment.mDocumento = documento;
        fragment.mOnPendenciaDialogListener = onPendenciaDialogListener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_pendencia, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        ButterKnife.bind(this, view);
        AlertDialog dialog = builder.create();
        populate();
        return dialog;
    }

    private void populate() {
        if (mDocumento != null) {
            FieldUtils.setText(mTitleTextView, mDocumento.irregularidadeNome);
            FieldUtils.setText(mObservacaoTextView, mDocumento.pendenciaObservacao);
        }
    }

    @OnClick(R.id.button_fechar)
    public void onFecharClick() {
        dismiss();
    }

    @OnClick(R.id.button_justificar)
    public void onJustificarClick() {
        String value = FieldUtils.getValue(mJustificativaEditText);
        if (StringUtils.isBlank(value)) {
            mJustificativaTextInputLayout.setError(getString(R.string.msg_required_field, getString(R.string.justificativa)));
        } else {
            dismiss();
            if (mOnPendenciaDialogListener != null) {
                mOnPendenciaDialogListener.onJustificar(value);
            }
        }
    }

    public static interface OnPendenciaDialogListener {
        void onJustificar(String justificativa);
    }
}
