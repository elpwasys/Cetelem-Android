package br.com.wasys.cetelem.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.todobom.opennotescanner.OpenNoteScannerActivity;
import com.viewpagerindicator.CirclePageIndicator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.Permission;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.ImagePageAdapter;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.model.ImagemModel;
import br.com.wasys.cetelem.service.DocumentoService;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.DateUtils;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentoEdicaoFragment extends CetelemFragment implements ViewPager.OnPageChangeListener {

    @BindView(R.id.text_view_data) TextView mDataTextView;
    @BindView(R.id.text_view_nome) TextView mNomeTextView;
    @BindView(R.id.text_view_status) TextView mStatusTextView;
    @BindView(R.id.text_view_versao) TextView mVersaoTextView;
    @BindView(R.id.image_view_status) ImageView mStatusImagemView;

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.indicator) CirclePageIndicator mCirclePageIndicator;

    private Long mId;
    private int mPosition = -1;
    private DocumentoModel mDocumento;
    private ImagePageAdapter mImagePageAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String KEY_ID = DocumentoEdicaoFragment.class.getName() + ".id"; // Pk do Documento

    public static DocumentoEdicaoFragment newInstance(Long id) {
        DocumentoEdicaoFragment fragment = new DocumentoEdicaoFragment();
        if (id != null) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_ID, id);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(KEY_ID)) {
                mId = bundle.getLong(KEY_ID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_documento_edicao, container, false);
        setTitle(R.string.titulo_documento);
        ButterKnife.bind(this, view);
        Context context = getContext();
        mImagePageAdapter = new ImagePageAdapter(getFragmentManager(), null);
        int pixels = AndroidUtils.toPixels(context, 16f);
        mViewPager.setPageMargin(pixels);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mImagePageAdapter);
        mCirclePageIndicator.setViewPager(mViewPager);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        obter();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                    ImagemModel model = ImagemModel.from(uri);
                    mImagePageAdapter.setModel(model);
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        if (ArrayUtils.isNotEmpty(grantResults)) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    granted = false;
                    break;
                }
            }
        }
        if (granted) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    openScanner();
                }
            }
        }
    }

    @OnClick(R.id.button_scan)
    public void onScanClick() {
        openScanner();
    }

    @OnClick(R.id.button_salvar)
    public void onSalvarClick() {

    }

    @OnClick(R.id.button_delete)
    public void onExclirClick() {
        FragmentActivity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.excluir)
                .setMessage(R.string.msg_deseja_excluir_registro)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void obter() {
        boolean granted = true;
        Context context = getBaseContext();
        String[] permitions = Permission.merge(Permission.STORAGE, Manifest.permission.CAMERA);
        for (String permission : permitions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }
        if (granted) {
            if (mId != null) {
                obter(mId);
            }
        } else {
            FragmentActivity activity = this.getActivity();
            ActivityCompat.requestPermissions(activity, permitions, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void popular(DocumentoModel documentoModel) {
        mDocumento = documentoModel;
        Context context = getContext();
        FieldUtils.setText(mDataTextView, mDocumento.dataDigitalizacao);
        FieldUtils.setText(mNomeTextView, mDocumento.nome);
        FieldUtils.setText(mVersaoTextView, mDocumento.versaoAtual);
        FieldUtils.setText(mStatusTextView, context.getString(mDocumento.status.stringRes));
        mStatusImagemView.setImageResource(mDocumento.status.drawableRes);
        if (CollectionUtils.isNotEmpty(documentoModel.imagens)) {
            mImagePageAdapter.setModels(documentoModel.imagens);
            mCirclePageIndicator.notifyDataSetChanged();
        }
    }

    private void openScanner() {
        boolean granted = true;
        Context context = getBaseContext();
        String[] permitions = Permission.merge(Permission.STORAGE, Manifest.permission.CAMERA);
        for (String permission : permitions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }
        if (granted) {
            if (AndroidUtils.isExternalStorageWritable()) {
                String applicationId = BuildConfig.APPLICATION_ID;
                File storage = new File(Environment.getExternalStorageDirectory(), applicationId);
                if (!storage.exists()) {
                    storage.mkdirs();
                }
                try {
                    Date date = new Date();
                    String name = DateUtils.format(date, "yyyyMMdd_HHmmssSSS.'jpg'");
                    String path = storage.getAbsolutePath() + File.separator + name;
                    File file = new File(path);
                    file.createNewFile();
                    Uri uri = Uri.fromFile(file);
                    Intent intent = new Intent(context, OpenNoteScannerActivity.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            FragmentActivity activity = this.getActivity();
            ActivityCompat.requestPermissions(activity, permitions, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void obter(Long id) {
        showProgress();
        Observable<DocumentoModel> observable = DocumentoService.Async.obter(id);
        prepare(observable).subscribe(new Subscriber<DocumentoModel>() {
            @Override
            public void onCompleted() {
                hideProgress();
            }
            @Override
            public void onError(Throwable e) {
                hideProgress();
                handle(e);
            }
            @Override
            public void onNext(DocumentoModel documentoModel) {
                hideProgress();
                popular(documentoModel);
            }
        });
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPosition = position;
    }
}
