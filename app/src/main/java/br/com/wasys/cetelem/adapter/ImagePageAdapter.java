package br.com.wasys.cetelem.adapter;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import br.com.wasys.cetelem.fragment.DocumentFragment;

/**
 * Created by pascke on 26/06/17.
 */

public class ImagePageAdapter extends FragmentStatePagerAdapter {

    private List<Uri> mUris;

    public ImagePageAdapter(FragmentManager fm, List<Uri> uris) {
        super(fm);
        mUris = uris;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (CollectionUtils.isNotEmpty(mUris)) {
            Uri uri= mUris.get(position);
            fragment = DocumentFragment.newInstance(uri);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return CollectionUtils.size(mUris);
    }
}