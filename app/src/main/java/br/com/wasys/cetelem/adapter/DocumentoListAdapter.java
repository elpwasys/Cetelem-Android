package br.com.wasys.cetelem.adapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 03/07/17.
 */

public class DocumentoListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<Group> mGroups;
    private LayoutInflater mInflater;
    private DocumentoListAdapterListener mDocumentoListAdapterListener;

    public DocumentoListAdapter(Context context, List<Group> groups) {
        this.mGroups = groups;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return CollectionUtils.size(mGroups);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Group group = (Group) getGroup(groupPosition);
        if (group != null) {
            return CollectionUtils.size(group.documentos);
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (CollectionUtils.isNotEmpty(mGroups)) {
            return mGroups.get(groupPosition);
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Group group = (Group) getGroup(groupPosition);
        if (group != null) {
            if (CollectionUtils.isNotEmpty(group.documentos)) {
                return group.documentos.get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView != null) {
            holder = (GroupViewHolder) convertView.getTag();
        }
        else {
            holder = new GroupViewHolder();
            convertView = mInflater.inflate(R.layout.list_group_documento, null);
            holder.nomeTextView = (TextView) convertView.findViewById(R.id.text_view_nome);
            convertView.setTag(holder);
        }
        Group group = mGroups.get(groupPosition);
        holder.nomeTextView.setText(mContext.getString(group.id));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView != null) {
            holder = (ChildViewHolder) convertView.getTag();
        }
        else {
            holder = new ChildViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_documento, null);
            holder.dataTextView = (TextView) convertView.findViewById(R.id.text_view_data);
            holder.nomeTextView = (TextView) convertView.findViewById(R.id.text_view_nome);
            holder.statusTextView = (TextView) convertView.findViewById(R.id.text_view_status);
            holder.versaoTextView = (TextView) convertView.findViewById(R.id.text_view_versao);
            holder.statusImagemView = (ImageView) convertView.findViewById(R.id.image_view_status);
            // PENDENCIA
            holder.pendenciaLayout = (LinearLayout) convertView.findViewById(R.id.layout_pendencia);
            holder.observacaoTextView = (TextView) convertView.findViewById(R.id.text_observacao);
            holder.irregularidadeTextView = (TextView) convertView.findViewById(R.id.text_irregularidade);
            holder.justificarImagem = (ImageView) convertView.findViewById(R.id.image_justificar);
            holder.justificarImagem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageView imageView = (ImageView) view;
                    DocumentoModel documento = (DocumentoModel) imageView.getTag();
                    if (mDocumentoListAdapterListener != null) {
                        mDocumentoListAdapterListener.onReplayClick(documento);
                    }
                }
            });
            convertView.setTag(holder);
        }
        Group group = mGroups.get(groupPosition);
        DocumentoModel documento = group.documentos.get(childPosition);
        FieldUtils.setText(holder.dataTextView, documento.dataDigitalizacao);
        FieldUtils.setText(holder.nomeTextView, documento.nome);
        FieldUtils.setText(holder.statusTextView, mContext.getString(documento.status.stringRes));
        FieldUtils.setText(holder.versaoTextView, mContext.getString(R.string.documento_versao, documento.versaoAtual));
        holder.statusImagemView.setImageResource(documento.status.drawableRes);
        // PENDENCIA
        holder.justificarImagem.setTag(documento);
        holder.pendenciaLayout.setVisibility(View.GONE);
        if (DocumentoModel.Status.PENDENTE.equals(documento.status)) {
            holder.pendenciaLayout.setVisibility(View.VISIBLE);
            FieldUtils.setText(holder.observacaoTextView, documento.pendenciaObservacao);
            FieldUtils.setText(holder.irregularidadeTextView, documento.irregularidadeNome);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setDocumentoListAdapterListener(DocumentoListAdapterListener documentoListAdapterListener) {
        mDocumentoListAdapterListener = documentoListAdapterListener;
    }

    public static class Group {
        private int id;
        private List<DocumentoModel> documentos;
        public Group(@StringRes int id, List<DocumentoModel> documentos) {
            this.id = id;
            this.documentos = documentos;
        }
        public DocumentoModel getAt(int index) {
            DocumentoModel model = null;
            if (CollectionUtils.size(documentos) > index) {
                model = CollectionUtils.get(documentos, index);
            }
            return model;
        }
    }

    static class GroupViewHolder {
        public TextView nomeTextView;
    }

    static class ChildViewHolder {
        public TextView dataTextView;
        public TextView nomeTextView;
        public TextView statusTextView;
        public TextView versaoTextView;
        public ImageView statusImagemView;
        // PENDENCIA
        public LinearLayout pendenciaLayout;
        public TextView observacaoTextView;
        public TextView irregularidadeTextView;
        public ImageView justificarImagem;
    }

    public static interface DocumentoListAdapterListener {
        void onReplayClick(DocumentoModel documento);
    }
}