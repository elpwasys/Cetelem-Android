package br.com.wasys.cetelem.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

import br.com.wasys.cetelem.R;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.ImageUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentFragment extends Fragment {

    private Uri mUri;

    @BindView(R.id.imageView) ImageView mImageView;

    private static final String KEY_URI = DocumentFragment.class.getName() + ".mUri";

    public static DocumentFragment newInstance(Uri uri) {
        DocumentFragment fragment = new DocumentFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_URI, uri);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUri = bundle.getParcelable(KEY_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mUri != null) {
            try {
                Context context = getContext();
                int width = AndroidUtils.getWidthPixels(context);
                Bitmap bitmap = ImageUtils.resize(mUri, width);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
