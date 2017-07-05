package br.com.wasys.cetelem.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.fragment.ImagemFragment;
import br.com.wasys.cetelem.model.ImagemModel;

/**
 * Created by pascke on 26/06/17.
 */

public class ImagePageAdapter extends FragmentStatePagerAdapter {

    private List<ImagemModel> mModels;

    public ImagePageAdapter(FragmentManager fm, List<ImagemModel> models) {
        super(fm);
        mModels = models;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (CollectionUtils.isNotEmpty(mModels)) {
            ImagemModel model = mModels.get(position);
            fragment = ImagemFragment.newInstance(model);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return CollectionUtils.size(mModels);
    }

    public List<ImagemModel> getModels() {
        return mModels;
    }

    public ImagemModel getModelAt(int position) {
        if (CollectionUtils.size(mModels) > position) {
            return mModels.get(position);
        }
        return null;
    }

    public void deleteModelAt(int position) {
        if (CollectionUtils.size(mModels) > position) {
            mModels.remove(position);
            notifyDataSetChanged();
        }
    }

    public void addModel(ImagemModel model) {
        if (mModels == null) {
            mModels = new ArrayList<>();
        }
        mModels.add(model);
        notifyDataSetChanged();
    }

    public void setModels(List<ImagemModel> models) {
        mModels = models;
        notifyDataSetChanged();
    }
}