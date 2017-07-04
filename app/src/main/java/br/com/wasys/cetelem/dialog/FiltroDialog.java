package br.com.wasys.cetelem.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.dataset.meta.ProcessoMeta;
import br.com.wasys.cetelem.model.FiltroModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.widget.AppDateEditText;
import br.com.wasys.cetelem.widget.AppNumberEditText;
import br.com.wasys.library.utils.TypeUtils;
import br.com.wasys.library.widget.AppSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pascke on 01/07/17.
 */

public class FiltroDialog extends AppCompatDialogFragment {

    @BindView(R.id.spinner_tipo) AppSpinner mTipoSpinner;
    @BindView(R.id.spinner_status) AppSpinner mStatusSpinner;
    @BindView(R.id.edit_numero) AppNumberEditText mNumeroEditText;
    @BindView(R.id.edit_inicio) AppDateEditText mInicioDateEditText;
    @BindView(R.id.edit_termino) AppDateEditText mTerminoDateEditText;

    private FiltroModel mFiltro;
    private ProcessoMeta mMeta;
    private OnFiltroListener mListener;

    public static FiltroDialog newInstance(ProcessoMeta meta, FiltroModel filtro, OnFiltroListener listener) {
        FiltroDialog dialog = new FiltroDialog();
        dialog.mMeta = meta;
        dialog.mFiltro = filtro;
        dialog.mListener = listener;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_filtro, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        ButterKnife.bind(this, view);
        if (mMeta != null) {
            mTipoSpinner.setOptions(mMeta.tiposProcessos);
        }
        List<ProcessoModel.Status> statusList = Arrays.asList(ProcessoModel.Status.values());
        mStatusSpinner.setOptions(statusList);
        if (mFiltro != null) {
            mNumeroEditText.setValue(mFiltro.numero);
            mInicioDateEditText.setValue(mFiltro.dataInicio);
            mTerminoDateEditText.setValue(mFiltro.dataTermino);
            if (mFiltro.status != null) {
                mStatusSpinner.setValue(mFiltro.status.name());
            }
            if (mFiltro.tipoProcessoId != null) {
                mTipoSpinner.setValue(String.valueOf(mFiltro.tipoProcessoId));
            }
        }
        return builder.create();
    }

    @OnClick(R.id.button_limpar)
    public void onLimparClick() {
        mFiltro = new FiltroModel();
        if (mListener != null) {
            mListener.onFiltro(mFiltro);
        }
        dismiss();
    }

    @OnClick(R.id.button_filtrar)
    public void onFiltrarClick() {
        if (mFiltro == null) {
            mFiltro = new FiltroModel();
        }
        mFiltro.numero = mNumeroEditText.getValue();
        mFiltro.tipoProcessoId = TypeUtils.parse(Long.class, mTipoSpinner.getValue());
        mFiltro.dataInicio = mInicioDateEditText.getValue();
        mFiltro.dataTermino = mTerminoDateEditText.getValue();
        mFiltro.status = TypeUtils.parse(ProcessoModel.Status.class, mStatusSpinner.getValue());
        if (mListener != null) {
            mListener.onFiltro(mFiltro);
        }
        dismiss();
    }

    public static interface OnFiltroListener {
        void onFiltro(FiltroModel filtroModel);
    }
}
