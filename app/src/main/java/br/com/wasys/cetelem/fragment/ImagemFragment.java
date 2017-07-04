package br.com.wasys.cetelem.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.todobom.opennotescanner.OpenNoteScannerActivity;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.ImagemModel;
import br.com.wasys.cetelem.service.ImagemService;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.DateUtils;
import br.com.wasys.library.utils.FileUtils;
import br.com.wasys.library.utils.ImageUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImagemFragment extends CetelemFragment {

    private ImagemModel mModel;

    @BindView(R.id.imageView) ImageView mImageView;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    private static final int REQUEST_VIEW = 1;
    private static final String KEY_MODEL = ImagemFragment.class.getName() + ".mModel";

    public static ImagemFragment newInstance(ImagemModel model) {
        ImagemFragment fragment = new ImagemFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_MODEL, model);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(KEY_MODEL)) {
                mModel = (ImagemModel) bundle.getSerializable(KEY_MODEL);
            }
        }
    }

    @OnClick(R.id.imageView)
    public void onImageClick() {
        Uri uri = ImagemService.getViewerUri(mModel.path);
        if (uri != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            startActivityForResult(intent, REQUEST_VIEW);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imagem, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciar();
    }

    private void iniciar() {
        configure(mModel);
    }

    private void configure(ImagemModel model) {
        mModel = model;
        if (StringUtils.isNotBlank(mModel.path)) {
            try {
                Uri uri = Uri.parse(mModel.path);
                Context context = getContext();
                int width = AndroidUtils.getWidthPixels(context);
                Bitmap bitmap = ImageUtils.resize(uri, width);
                mImageView.setImageBitmap(bitmap);
                mProgressBar.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                handle(e);
            }
        } else if (StringUtils.isNotBlank(mModel.caminho)) {
            String caminho = mModel.caminho;
            if (caminho.startsWith("/")) {
                caminho = caminho.substring(1);
                carregar(caminho);
            }
        }
    }

    private void carregar(String caminho) {
        mImageView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        Observable<ImagemModel> observable = ImagemService.Async.carregar(caminho);
        prepare(observable).subscribe(new Subscriber<ImagemModel>() {
            @Override
            public void onCompleted() {
                mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Throwable e) {
                mProgressBar.setVisibility(View.GONE);
                handle(e);
            }
            @Override
            public void onNext(ImagemModel model) {
                mProgressBar.setVisibility(View.GONE);
                configure(model);
            }
        });
    }
}
