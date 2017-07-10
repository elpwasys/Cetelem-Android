package br.com.wasys.cetelem.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.activity.DocumentScannerActivity;
import br.com.wasys.cetelem.adapter.DocumentoListAdapter;
import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.DocumentoMeta;
import br.com.wasys.cetelem.dialog.PendenciaDialog;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.model.JustificativaModel;
import br.com.wasys.cetelem.model.ProcessoLogModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.ProcessoRegraModel;
import br.com.wasys.cetelem.model.ResultModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.service.DigitalizacaoService;
import br.com.wasys.cetelem.service.DocumentoService;
import br.com.wasys.cetelem.service.ProcessoService;
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
public class DocumentoListaFragment extends CetelemFragment implements ExpandableListView.OnChildClickListener, DocumentoListAdapter.DocumentoListAdapterListener {

    @BindView(R.id.list_view) ExpandableListView mListView;
    @BindView(R.id.text_pendencia) TextView mPendenciaTextView;
    @BindView(R.id.button_enviar) FloatingActionButton mEnviarFloatingActionButton;
    @BindView(R.id.button_reenviar) FloatingActionButton mReenviarFloatingActionButton;

    private Long mId;

    private ProcessoRegraModel mRegra;
    private ArrayList<Uri> mUris;

    private List<DocumentoListAdapter.Group> mGroups;

    private static final int REQUEST_SCAN = 1;
    private static final String KEY_ID = DocumentoListaFragment.class.getName() + ".id"; // Pk do Processo
    private MenuItem mMenuItemAdd;

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
        mMenuItemAdd = menu.findItem(R.id.action_add);
        mMenuItemAdd.setVisible(false);
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
        DocumentoDetalheFragment fragment = DocumentoDetalheFragment.newInstance(documentoModel.id);
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

    @OnClick(R.id.button_enviar)
    public void onEviarClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.enviar)
                .setMessage(R.string.msg_enviar_processo)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAsyncEnviar(mId);
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

    @OnClick(R.id.button_reenviar)
    public void onReeviarClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.reenviar)
                .setMessage(R.string.msg_reenviar_processo)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAsyncReenviar(mId);
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
    private void onAsyncDataSetCompleted(DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet) {

        DocumentoMeta meta = dataSet.meta;
        mRegra = meta.regra;

        mMenuItemAdd.setVisible(false);
        if (mRegra.podeDigitalizar) {
            mMenuItemAdd.setVisible(true);
        }

        ProcessoLogModel log = meta.log;
        mPendenciaTextView.setVisibility(View.GONE);
        if (log != null) {
            FieldUtils.setText(mPendenciaTextView, log.observacao);
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

        mEnviarFloatingActionButton.setVisibility(View.GONE);
        if (mRegra.podeEnviar && MapUtils.isEmpty(mRegra.pendencias)) {
            mEnviarFloatingActionButton.setVisibility(View.VISIBLE);
        }

        mReenviarFloatingActionButton.setVisibility(View.GONE);
        if (mRegra.podeResponderPendencia && MapUtils.isEmpty(mRegra.pendencias)) {
            mReenviarFloatingActionButton.setVisibility(View.VISIBLE);
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
            Observable<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> observable = DocumentoService.Async.getDataSet(mId);
            prepare(observable).subscribe(new Subscriber<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>>() {
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
                public void onNext(DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet) {
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

    private void startAsyncEnviar(Long id) {
        if (mId != null) {
            showProgress();
            Observable<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> observable = DocumentoService.Async.enviar(mId);
            prepare(observable).subscribe(new Subscriber<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>>() {
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
                public void onNext(DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet) {
                    hideProgress();
                    Toast.makeText(getContext(), R.string.msg_processo_enviado_sucesso, Toast.LENGTH_SHORT).show();
                    onAsyncDataSetCompleted(dataSet);
                }
            });
        }
    }

    private void startAsyncReenviar(Long id) {
        if (mId != null) {
            showProgress();
            Observable<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>> observable = DocumentoService.Async.reenviar(mId);
            prepare(observable).subscribe(new Subscriber<DataSet<ArrayList<DocumentoModel>, DocumentoMeta>>() {
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
                public void onNext(DataSet<ArrayList<DocumentoModel>, DocumentoMeta> dataSet) {
                    hideProgress();
                    Toast.makeText(getContext(), R.string.msg_processo_reenviado_sucesso, Toast.LENGTH_SHORT).show();
                    onAsyncDataSetCompleted(dataSet);
                }
            });
        }
    }
}