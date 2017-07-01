package br.com.wasys.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by pascke on 20/04/16.
 */
public abstract class ListAdapter<T> extends BaseAdapter {

    protected List<T> mRows;
    protected LayoutInflater mInflater;

    public ListAdapter(Context context, List<T> rows) {
        this.mRows = rows;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return CollectionUtils.size(mRows);
    }

    @Override
    public T getItem(int position) {
        return mRows != null ? mRows.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setRows(List<T> rows) {
        mRows = rows;
        notifyDataSetChanged();
    }
}
