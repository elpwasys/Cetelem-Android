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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.todobom.opennotescanner.OpenNoteScannerActivity;
import com.viewpagerindicator.CirclePageIndicator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.Permission;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.ImagePageAdapter;
import br.com.wasys.cetelem.dialog.DigitalizacaoDialog;
import br.com.wasys.cetelem.dialog.PendenciaDialog;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.model.ImagemModel;
import br.com.wasys.cetelem.model.JustificativaModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.service.DigitalizacaoService;
import br.com.wasys.cetelem.service.DocumentoService;
import br.com.wasys.cetelem.service.ImagemService;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.DateUtils;
import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.FragmentUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;

import static br.com.wasys.cetelem.background.DigitalizacaoService.startDigitalizacaoService;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentoDetalheFragment extends CetelemFragment implements ViewPager.OnPageChangeListener {

    @BindView(R.id.text_view_data) TextView mDataTextView;
    @BindView(R.id.text_view_nome) TextView mNomeTextView;
    @BindView(R.id.text_view_status) TextView mStatusTextView;
    @BindView(R.id.text_view_versao) TextView mVersaoTextView;
    @BindView(R.id.image_view_status) ImageView mStatusImagemView;

    // PENDENCIA
    @BindView(R.id.text_observacao) TextView mObservacaoTextView;
    @BindView(R.id.image_justificar) ImageView mJustificarImagem;
    @BindView(R.id.layout_pendencia) LinearLayout mPendenciaLayout;
    @BindView(R.id.text_irregularidade) TextView mIrregularidadeTextView;

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.indicator) CirclePageIndicator mCirclePageIndicator;
    @BindView(R.id.button_scan) FloatingActionButton mScanFloatingActionButton;
    @BindView(R.id.button_salvar) FloatingActionButton mSalvarFloatingActionButton;
    @BindView(R.id.button_delete) FloatingActionButton mDeleteFloatingActionButton;

    private Long mId;
    private int mPosition = -1;
    private DocumentoModel mDocumento;
    private DigitalizacaoModel mDigitalizacao;

    private ImagePageAdapter mImagePageAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String KEY_ID = DocumentoDetalheFragment.class.getName() + ".id"; // Pk do Documento

    public static DocumentoDetalheFragment newInstance(Long id) {
        DocumentoDetalheFragment fragment = new DocumentoDetalheFragment();
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
        View view = inflater.inflate(R.layout.fragment_documento_detalhe, container, false);
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
        startAsyncObter();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                    ImagemModel model = ImagemModel.from(uri);
                    mImagePageAdapter.addModel(model);
                    setVisibilityActions();
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

    @OnClick(R.id.button_info)
    public void onInfoClick() {
        startAsyncDigitalizacaoBy(mId);
    }

    @OnClick(R.id.button_salvar)
    public void onSalvarClick() {
        List<ImagemModel> imagens = mImagePageAdapter.getModels();
        if (CollectionUtils.isNotEmpty(imagens)) {
            List<UploadModel> uploads = new ArrayList<>();
            for (ImagemModel imagem : imagens) {
                if (imagem.id == null) {
                    uploads.add(new UploadModel(imagem.path));
                }
            }
            if (CollectionUtils.isNotEmpty(uploads)) {
                startAsyncSalvar(uploads);
            }
        }
    }

    @OnClick(R.id.button_delete)
    public void onExclirClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.excluir)
                .setMessage(R.string.msg_deseja_excluir_registro)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        excluir();
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

    private void excluir() {
        ImagemModel model = mImagePageAdapter.getModelAt(mPosition);
        if (model.id != null) {
            startAsyncExcluir(model.id);
        } else {
            mImagePageAdapter.deleteModelAt(mPosition);
        }
    }

    @OnClick(R.id.image_justificar)
    public void onJustificarClick() {
        PendenciaDialog dialog = PendenciaDialog.newInstance(mDocumento, new PendenciaDialog.OnPendenciaDialogListener() {
            @Override
            public void onJustificar(String justificativa) {
                JustificativaModel justificativaModel = new JustificativaModel();
                justificativaModel.id = mDocumento.id;
                justificativaModel.texto = justificativa;
                startAsyncJustificar(justificativaModel);
            }
        });
        FragmentManager manager = getFragmentManager();
        dialog.show(manager, dialog.getClass().getSimpleName());
    }

    private void startAsyncObter() {
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
                startAsyncObter(mId);
            }
        } else {
            FragmentActivity activity = this.getActivity();
            ActivityCompat.requestPermissions(activity, permitions, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setVisibilityActions() {
        mSalvarFloatingActionButton.setVisibility(View.GONE);
        if (BooleanUtils.isTrue(mDocumento.digitalizavel)) {
            boolean has = mImagePageAdapter.hasUpload();
            if (has) {
                mSalvarFloatingActionButton.setVisibility(View.VISIBLE);
            }
        }
        mDeleteFloatingActionButton.setVisibility(View.GONE);
        if (BooleanUtils.isTrue(mDocumento.podeExcluir)) {
            if (mImagePageAdapter.getCount() > 0) {
                mDeleteFloatingActionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onAsyncObterCompleted(DocumentoModel documentoModel) {
        mDocumento = documentoModel;
        Context context = getContext();
        DocumentoModel.Status status = mDocumento.status;
        FieldUtils.setText(mDataTextView, mDocumento.dataDigitalizacao);
        FieldUtils.setText(mNomeTextView, mDocumento.nome);
        FieldUtils.setText(mVersaoTextView, mDocumento.versaoAtual);
        FieldUtils.setText(mVersaoTextView, getString(R.string.documento_versao, mDocumento.versaoAtual));
        FieldUtils.setText(mStatusTextView, context.getString(status.stringRes));
        mStatusImagemView.setImageResource(status.drawableRes);
        if (CollectionUtils.isNotEmpty(mDocumento.imagens)) {
            mImagePageAdapter.setModels(mDocumento.imagens);
            mCirclePageIndicator.notifyDataSetChanged();
        }
        mPendenciaLayout.setVisibility(View.GONE);
        if (DocumentoModel.Status.PENDENTE.equals(status)) {
            mPendenciaLayout.setVisibility(View.VISIBLE);
            FieldUtils.setText(mObservacaoTextView, mDocumento.pendenciaObservacao);
            FieldUtils.setText(mIrregularidadeTextView, mDocumento.irregularidadeNome);
        }
        mScanFloatingActionButton.setVisibility(View.GONE);
        if (BooleanUtils.isTrue(mDocumento.digitalizavel)) {
            mScanFloatingActionButton.setVisibility(View.VISIBLE);
        }
        setVisibilityActions();
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

    private void reenviar() {
        String referencia = String.valueOf(mId);
        DigitalizacaoModel.Tipo tipo = DigitalizacaoModel.Tipo.DOCUMENTO;
        startDigitalizacaoService(getContext(), tipo, referencia);
        Toast.makeText(getContext(), R.string.msg_documento_reenviado, Toast.LENGTH_LONG).show();
    }

    private void onAsyncExcluirCompleted(ResultModel resultModel) {
        if (resultModel.success) {
            mImagePageAdapter.deleteModelAt(mPosition);
            int count = mImagePageAdapter.getCount();
            if (count < 1) {
                mImagePageAdapter = new ImagePageAdapter(getFragmentManager(), null);
                mViewPager.setAdapter(mImagePageAdapter);
                mCirclePageIndicator.setViewPager(mViewPager);
            }
            Snackbar.make(mDeleteFloatingActionButton, getString(R.string.msg_imagem_excluida_sucesso), Snackbar.LENGTH_LONG).show();
        }
        setVisibilityActions();
    }

    private void onAsyncJustificarCompleted(ResultModel model) {
        Toast.makeText(getContext(), R.string.msg_justificativa_sucesso, Toast.LENGTH_LONG).show();
        FragmentUtils.popBackStackImmediate(getActivity(), getBackStackName());
    }

    private void onAsyncDigitalizacaoCompleted(DigitalizacaoModel model) {
        mDigitalizacao = model;
        if (mDigitalizacao == null) {
            Toast.makeText(getContext(), R.string.msg_sem_info_digitalizacao, Toast.LENGTH_SHORT).show();
        } else {
            DigitalizacaoDialog dialog = DigitalizacaoDialog.newInstance(mDigitalizacao, new DigitalizacaoDialog.OnUplodErrorListener() {
                @Override
                public void onReenviar(boolean answer) {
                    if (answer) {
                        reenviar();
                    }
                }
            });
            FragmentManager fragmentManager = getFragmentManager();
            dialog.show(fragmentManager, dialog.getClass().getSimpleName());
        }
    }

    private void onAsyncSalvar(DigitalizacaoModel model) {
        if (model != null) {
            Context context = getContext();
            startDigitalizacaoService(context, model.tipo, model.referencia);
            Toast.makeText(getContext(), R.string.msg_documento_enviado_sucesso, Toast.LENGTH_LONG).show();
            FragmentUtils.popBackStackImmediate(getActivity(), getBackStackName());
        }
    }

    private void startAsyncObter(Long id) {
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
                onAsyncObterCompleted(documentoModel);
            }
        });
    }

    private void startAsyncExcluir(Long id) {
        showProgress();
        Observable<ResultModel> observable = ImagemService.Async.excluir(id);
        prepare(observable).subscribe(new Subscriber<ResultModel>() {
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
            public void onNext(ResultModel resultModel) {
                hideProgress();
                onAsyncExcluirCompleted(resultModel);
            }
        });
    }

    private void startAsyncSalvar(List<UploadModel> uploads) {
        showProgress();
        String referencia = String.valueOf(mId);
        Observable<DigitalizacaoModel> observable = DigitalizacaoService.Async.criar(referencia, DigitalizacaoModel.Tipo.DOCUMENTO, uploads);
        prepare(observable).subscribe(new Subscriber<DigitalizacaoModel>() {
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
            public void onNext(DigitalizacaoModel model) {
                hideProgress();
                onAsyncSalvar(model);
            }
        });
    }

    private void startAsyncJustificar(JustificativaModel justificativaModel) {
        showProgress();
        Observable<ResultModel> observable = DocumentoService.Async.justificar(justificativaModel);
        prepare(observable).subscribe(new Subscriber<ResultModel>() {
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
            public void onNext(ResultModel model) {
                hideProgress();
                onAsyncJustificarCompleted(model);
            }
        });
    }

    private void startAsyncDigitalizacaoBy(Long id) {
        String referencia = String.valueOf(id);
        Observable<DigitalizacaoModel> observable = DigitalizacaoService.Async.getBy(referencia, DigitalizacaoModel.Tipo.DOCUMENTO);
        prepare(observable).subscribe(new Subscriber<DigitalizacaoModel>() {
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable e) {
                handle(e);
            }
            @Override
            public void onNext(DigitalizacaoModel model) {
                onAsyncDigitalizacaoCompleted(model);
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
