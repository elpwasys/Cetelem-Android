package br.com.wasys.cetelem.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.BooleanUtils;

import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.dialog.DocumentoDialog;
import br.com.wasys.library.adapter.ListAdapter;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 09/06/16.
 */
public class DocumentoAdapter extends ListAdapter<DocumentoDialog.Documento> {

    private Context mContext;

    public DocumentoAdapter(Context context, List<DocumentoDialog.Documento> rows) {
        super(context, rows);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        }
        else {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_dialog_documento, null);
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        }
        DocumentoDialog.Documento documento = mRows.get(position);
        FieldUtils.setText(holder.textView, documento.getLabel());
        if (BooleanUtils.isTrue(documento.getObrigatorio())) {
            holder.imageView.setImageResource(R.drawable.ic_radio_unchecked);
            ColorStateList colorStateList = ResourcesCompat.getColorStateList(mContext.getResources(), R.color.red_500, null);
            holder.imageView.setImageTintList(colorStateList);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_radio_unchecked);
            ColorStateList colorStateList = ResourcesCompat.getColorStateList(mContext.getResources(), R.color.grey_500, null);
            holder.imageView.setImageTintList(colorStateList);
        }
        return convertView;
    }

    static class ViewHolder {
        public TextView textView;
        public ImageView imageView;
    }
}
