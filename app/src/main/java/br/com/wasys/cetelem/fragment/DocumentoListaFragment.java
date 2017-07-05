package br.com.wasys.cetelem.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.activity.DocumentScannerActivity;
import br.com.wasys.cetelem.adapter.DocumentoListAdapter;
import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dialog.PendenciaDialog;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.model.JustificativaModel;
import br.com.wasys.cetelem.model.ProcessoLogModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.service.DigitalizacaoService;
import br.com.wasys.cetelem.service.DocumentoService;
import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.FragmentUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;

import static br.com.wasys.cetelem.background.DigitalizacaoService.startDigitalizacaoService;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentoListaFragment extends CetelemFragment implements ExpandableListView.OnChildClickListener, DocumentoListAdapter.DocumentoListAdapterListener {

    @BindView(R.id.list_view) ExpandableListView mListView;
    @BindView(R.id.text_pendencia) TextView mPendenciaTextView;

    private Long mId;
    private ArrayList<Uri> mUris;
    private List<DocumentoListAdapter.Group> mGroups;

    private static final int REQUEST_SCAN = 1;
    private static final String KEY_ID = DocumentoListaFragment.class.getName() + ".id"; // Pk do Processo

    public static DocumentoListaFragment newInstance(Long id) {
        DocumentoListaFragment fragment = new DocumentoListaFragment();
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
        View view = inflater.inflate(R.layout.fragment_documento_lista, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUris = new ArrayList<>();
        mListView.setOnChildClickListener(this);
        startAsyncDataSet();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_documento_lista, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_add:
                openScanner();
                return true;
            case R.id.action_refresh:
                startAsyncDataSet();
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SCAN: {
                    mUris = intent.getParcelableArrayListExtra(MediaStore.EXTRA_OUTPUT);
                    if (mUris == null) {
                        mUris = new ArrayList<>();
                    }
                    startAsyncSalvar();
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            deleteFiles();
        }
    }

    // LISTENER PARA OBTER O DOCUMENTO DA LISTA SELECIONADO
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        DocumentoListAdapter.Group group = mGroups.get(groupPosition);
        DocumentoModel documentoModel = group.getAt(childPosition);
        DocumentoEdicaoFragment fragment = DocumentoEdicaoFragment.newInstance(documentoModel.id);
        FragmentUtils.replace(getActivity(), R.id.content_main, fragment, fragment.getClass().getSimpleName());
        return true;
    }

    @Override
    public void onReplayClick(final DocumentoModel documento) {
        PendenciaDialog dialog = PendenciaDialog.newInstance(documento, new PendenciaDialog.OnPendenciaDialogListener() {
            @Override
            public void onJustificar(String justificativa) {
                JustificativaModel justificativaModel = new JustificativaModel();
                justificativaModel.id = documento.id;
                justificativaModel.texto = justificativa;
                startAsyncJustificar(justificativaModel);
            }
        });
        FragmentManager manager = getFragmentManager();
        dialog.show(manager, dialog.getClass().getSimpleName());
    }

    private void deleteFiles() {
        if (CollectionUtils.isNotEmpty(mUris)) {
            for (Uri uri : mUris) {
                String path = uri.getPath();
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        mUris.clear();
    }

    // ABRE O SCANNER
    private void openScanner() {
        Context context = getContext();
        Intent intent = DocumentScannerActivity.newIntent(context, new ArrayList<>(mUris), null);
        startActivityForResult(intent, REQUEST_SCAN);
    }

    // POPULA A LISTA DE DOCUMENTOS
    private void onAsyncDataSetCompleted(DataSet<ArrayList<DocumentoModel>, ProcessoLogModel> dataSet) {

        ProcessoLogModel meta = dataSet.meta;
        if (meta != null) {
            FieldUtils.setText(mPendenciaTextView, meta.observacao);
            mPendenciaTextView.setVisibility(View.VISIBLE);
        }

        ArrayList<DocumentoModel> data = dataSet.data;
        List<DocumentoModel> opcionais = new ArrayList<>();
        List<DocumentoModel> obrigatorios = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            for (DocumentoModel model : data) {
                if (BooleanUtils.isTrue(model.obrigatorio)) {
                    obrigatorios.add(model);
                } else {
                    opcionais.add(model);
                }
            }
        }
        mGroups = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(obrigatorios)) {
            mGroups.add(new DocumentoListAdapter.Group(R.string.obrigatorios, obrigatorios));
        }
        if (CollectionUtils.isNotEmpty(opcionais)) {
            mGroups.add(new DocumentoListAdapter.Group(R.string.opcionais, opcionais));
        }
        DocumentoListAdapter adapter = new DocumentoListAdapter(getContext(), mGroups);
        adapter.setDocumentoListAdapterListener(this);
        mListView.setAdapter(adapter);
        for (int i = 0; i < mGroups.size(); i++) {
            mListView.expandGroup(i);
        }
    }

    private void onAsyncJustificarCompleted(ResultModel model) {
        Toast.makeText(getContext(), R.string.msg_justificativa_sucesso, Toast.LENGTH_LONG).show();
        startAsyncDataSet();
    }

    private void asyncSalvarCompleted(DigitalizacaoModel model) {
        if (model != null) {
            startDigitalizacaoService(getContext(), model.tipo, model.referencia);
            Snackbar.make(getView(), getString(R.string.msg_processo_salvo_sucesso), Snackbar.LENGTH_LONG).show();
            startAsyncDataSet();
        }
    }

    // BUSCA A LISTA DE DOCUMENTOS NO SERVIDOR
    private void startAsyncDataSet() {
        if (mId != null) {
            showProgress();
            Observable<DataSet<ArrayList<DocumentoModel>, ProcessoLogModel>> observable = DocumentoService.Async.getDataSet(mId);
            prepare(observable).subscribe(new Subscriber<DataSet<ArrayList<DocumentoModel>, ProcessoLogModel>>() {
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
                public void onNext(DataSet<ArrayList<DocumentoModel>, ProcessoLogModel> dataSet) {
                    hideProgress();
                    onAsyncDataSetCompleted(dataSet);
                }
            });
        }
    }

    private void startAsyncSalvar() {
        if (CollectionUtils.isNotEmpty(mUris)) {
            ArrayList<UploadModel> uploads = new ArrayList<>(mUris.size());
            for (Uri mUri : mUris) {
                String path = mUri.getPath();
                uploads.add(new UploadModel(path));
            }
            showProgress();
            String referencia = String.valueOf(mId);
            Observable<DigitalizacaoModel> observable = DigitalizacaoService.Async.criar(referencia, DigitalizacaoModel.Tipo.TIPIFICACAO, uploads);
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
                    asyncSalvarCompleted(model);
                }
            });
        }
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
}