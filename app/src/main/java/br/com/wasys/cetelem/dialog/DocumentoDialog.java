package br.com.wasys.cetelem.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.DocumentoAdapter;
import br.com.wasys.cetelem.model.TipoDocumentoModel;

/**
 * Created by pascke on 28/06/17.
 */

public class DocumentoDialog extends AppCompatDialogFragment {

    private CheckBox mCheckBox;

    private boolean mDisplayMore;
    private List<Documento> mDocumentos;
    private OnDismissListener mOnDismissListener;

    public static DocumentoDialog newInstance(List<TipoDocumentoModel> models, OnDismissListener onDismissListener) {
        return newInstance(models, onDismissListener, true);
    }

    public static DocumentoDialog newInstance(List<TipoDocumentoModel> models, OnDismissListener onDismissListener, boolean mDisplayMore) {
        DocumentoDialog dialog = new DocumentoDialog();
        if (CollectionUtils.isNotEmpty(models)) {
            dialog.mDisplayMore = mDisplayMore;
            dialog.mDocumentos = new ArrayList<>(models.size());
            dialog.mOnDismissListener = onDismissListener;
            for (TipoDocumentoModel model : models) {
                dialog.mDocumentos.add(new Documento(model.nome, model.obrigatorio));
            }
        }
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        Context context = activity.getBaseContext();
        LayoutInflater inflater = LayoutInflater.from(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_documento, null);
        mCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
        if (!mDisplayMore) {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layout_displayMore);
            linearLayout.setVisibility(View.INVISIBLE);
        }
        ListView listView = (ListView) view.findViewById(R.id.documento_list_view);
        listView.setAdapter(new DocumentoAdapter(context, mDocumentos));
        builder.setView(view);
        builder.setTitle(R.string.documentos);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleOnDismissListener(true);
            }
        });
        builder.setNegativeButton(R.string.fechar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleOnDismissListener(false);
            }
        });
        return builder.create();
    }

    private void handleOnDismissListener(boolean answer) {
        if (mOnDismissListener != null) {
            boolean displayMore = !mCheckBox.isChecked();
            mOnDismissListener.onDismiss(answer, displayMore);
        }
    }

    public static interface OnDismissListener {
        void onDismiss(boolean answer, boolean displayMore);
    }

    public static class Documento implements Serializable {
        private String label;
        private Boolean obrigatorio;
        public Documento(String label, Boolean obrigatorio) {
            this.label = label;
            this.obrigatorio = obrigatorio;
        }
        public String getLabel() {
            return label;
        }
        public Boolean getObrigatorio() {
            return obrigatorio;
        }
    }
}
