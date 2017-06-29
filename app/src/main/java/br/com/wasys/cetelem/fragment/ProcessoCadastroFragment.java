package br.com.wasys.cetelem.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.activity.DocumentScanActivity;
import br.com.wasys.cetelem.dataset.DataSet;
import br.com.wasys.cetelem.dataset.meta.ProcessoMeta;
import br.com.wasys.cetelem.dataset.meta.TipoProcessoMeta;
import br.com.wasys.cetelem.model.CampoGrupoModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.TipoDocumentoModel;
import br.com.wasys.cetelem.model.TipoProcessoModel;
import br.com.wasys.cetelem.model.UploadModel;
import br.com.wasys.cetelem.service.DigitalizacaoService;
import br.com.wasys.cetelem.service.ProcessoService;
import br.com.wasys.cetelem.widget.AppCampoGrupoLayout;
import br.com.wasys.library.utils.PreferencesUtils;
import br.com.wasys.library.utils.TypeUtils;
import br.com.wasys.library.widget.AppSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 24/06/17.
 */

public class ProcessoCadastroFragment extends CetelemFragment {

    @BindView(R.id.layout_fields) LinearLayout mLayoutFields;
    @BindView(R.id.button_salvar) FloatingActionButton mButtonSalvar;
    @BindView(R.id.spinner_tipo_processo) AppSpinner mSpinnerTipoProcesso;
    @BindView(R.id.layout_spinner) TextInputLayout mSpinnerTextInputLayout;

    private Long mId;
    private ArrayList<Uri> mUris;
    private DataSet<ProcessoModel, ProcessoMeta> mProcessoDataSet;
    private DataSet<TipoProcessoModel, TipoProcessoMeta> mTipoProcessoDataSet;

    private static final int REQUEST_SCAN = 1;

    private static final String KEY_ID = ProcessoCadastroFragment.class.getName() + ".id";
    private static final String KEY_URIS = ProcessoCadastroFragment.class.getName() + ".mUris";
    private static final String KEY_PROCESSO_DATA_SET = ProcessoCadastroFragment.class.getName() + ".mProcessoDataSet";
    private static final String KEY_TIPO_PROCESSO_DATA_SET = ProcessoCadastroFragment.class.getName() + ".mTipoProcessoDataSet";

    public static ProcessoCadastroFragment newInstance() {
        return newInstance(null);
    }

    public static ProcessoCadastroFragment newInstance(Long id) {
        ProcessoCadastroFragment fragment = new ProcessoCadastroFragment();
        if (id != null) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_ID, id);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(KEY_ID)) {
                mId = bundle.getLong(KEY_ID);
            }
            if (savedInstanceState.containsKey(KEY_ID)) {
                mId = savedInstanceState.getLong(KEY_ID);
            }
            if (savedInstanceState.containsKey(KEY_URIS)) {
                mUris = savedInstanceState.getParcelableArrayList(KEY_URIS);
            }
            if (savedInstanceState.containsKey(KEY_PROCESSO_DATA_SET)) {
                mProcessoDataSet = (DataSet<ProcessoModel, ProcessoMeta>) savedInstanceState.getSerializable(KEY_PROCESSO_DATA_SET);
            }
            if (savedInstanceState.containsKey(KEY_TIPO_PROCESSO_DATA_SET)) {
                mTipoProcessoDataSet = (DataSet<TipoProcessoModel, TipoProcessoMeta>) savedInstanceState.getSerializable(KEY_TIPO_PROCESSO_DATA_SET);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_processo_cadastro, container, false);
        setTitle(R.string.titulo_processo);
        mUris = new ArrayList<>();
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mId != null) {
            outState.putLong(KEY_ID, mId);
        }
        if (mUris != null) {
            outState.putParcelableArrayList(KEY_URIS, mUris);
        }
        if (mProcessoDataSet != null) {
            outState.putSerializable(KEY_PROCESSO_DATA_SET, mProcessoDataSet);
        }
        if (mTipoProcessoDataSet != null) {
            outState.putSerializable(KEY_TIPO_PROCESSO_DATA_SET, mTipoProcessoDataSet);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_ID)) {
                mId = savedInstanceState.getLong(KEY_ID);
            }
            if (savedInstanceState.containsKey(KEY_URIS)) {
                mUris = savedInstanceState.getParcelableArrayList(KEY_URIS);
            }
            if (savedInstanceState.containsKey(KEY_PROCESSO_DATA_SET)) {
                mProcessoDataSet = (DataSet<ProcessoModel, ProcessoMeta>) savedInstanceState.getSerializable(KEY_PROCESSO_DATA_SET);
            }
            if (savedInstanceState.containsKey(KEY_TIPO_PROCESSO_DATA_SET)) {
                mTipoProcessoDataSet = (DataSet<TipoProcessoModel, TipoProcessoMeta>) savedInstanceState.getSerializable(KEY_TIPO_PROCESSO_DATA_SET);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        iniciar();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpinnerTipoProcesso.setOnOptionClickListener(new AppSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(String value) {
                mSpinnerTextInputLayout.setError(null);
                mSpinnerTextInputLayout.setErrorEnabled(false);
                onTipoDataSetLoad(value);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_processo_cadastro, menu);
        MenuItem item;
        if (mId != null) {
            item = menu.findItem(R.id.action_photo);
        } else {
            item = menu.findItem(R.id.action_collection);
        }
        item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_photo:
                openScan();
                return true;
            case R.id.action_collection:
                // ABRIR DOCUMENTOS
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SCAN: {
                    ArrayList<Uri> uris = intent.getParcelableArrayListExtra(MediaStore.EXTRA_OUTPUT);
                    mUris = uris;
                    if (mUris == null) {
                        mUris = new ArrayList<>();
                    }
                    salvar();
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            // nothing for now
        }
    }

    @OnClick(R.id.button_salvar)
    public void onSalvarClick() {
        salvar();
    }

    private void openScan() {
        Context context = getContext();
        ArrayList<TipoDocumentoModel> documentos = null;
        if (mTipoProcessoDataSet != null) {
            documentos = mTipoProcessoDataSet.meta.tiposDocumentos;
        }
        Intent intent = DocumentScanActivity.newIntent(context, new ArrayList<>(mUris), documentos);
        startActivityForResult(intent, REQUEST_SCAN);
    }

    private void iniciar() {
        if (mProcessoDataSet == null) {
            onDataSetLoad();
        } else {
            onDataSetLoaded(mProcessoDataSet);
        }
    }

    private void onDataSetLoad() {
        showProgress();
        Observable<DataSet<ProcessoModel, ProcessoMeta>> observable = ProcessoService.Async.getDataSet();
        prepare(observable).subscribe(new Subscriber<DataSet<ProcessoModel, ProcessoMeta>>() {
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
            public void onNext(DataSet<ProcessoModel, ProcessoMeta> dataSet) {
                hideProgress();
                onDataSetLoaded(dataSet);
            }
        });
    }

    private void onTipoDataSetLoad(String value) {
        Long id = TypeUtils.parse(Long.class, value);
        mButtonSalvar.setVisibility(View.GONE);
        if (id != null) {
            showProgress();
            Observable<DataSet<TipoProcessoModel, TipoProcessoMeta>> observable = ProcessoService.Async.getTipoDataSet(id);
            prepare(observable).subscribe(new Subscriber<DataSet<TipoProcessoModel, TipoProcessoMeta>>() {
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
                public void onNext(DataSet<TipoProcessoModel, TipoProcessoMeta> dataSet) {
                    hideProgress();
                    onTipoDataSetLoaded(dataSet);
                }
            });
        }
    }

    private void onDataSetLoaded(DataSet<ProcessoModel, ProcessoMeta> dataSet) {
        mProcessoDataSet = dataSet;
        if (mProcessoDataSet != null) {
            ProcessoMeta meta = mProcessoDataSet.meta;
            ProcessoModel model = dataSet.data;
            mSpinnerTipoProcesso.setOptions(meta.tiposProcessos);
            if (model != null) {
                TipoProcessoModel tipoProcesso = model.tipoProcesso;
                if (tipoProcesso != null) {
                    // TODO configurar Tipo de Processo no AppSpinner
                }
            }
        }
        int childCount = mLayoutFields.getChildCount();
        if (childCount == 0) {
            mSpinnerTipoProcesso.showDropDown();
        }
    }

    private void onTipoDataSetLoaded(DataSet<TipoProcessoModel, TipoProcessoMeta> dataSet) {
        mLayoutFields.removeAllViews();
        mTipoProcessoDataSet = dataSet;
        if (mTipoProcessoDataSet != null) {
            TipoProcessoMeta meta = mTipoProcessoDataSet.meta;
            if (CollectionUtils.isNotEmpty(meta.camposGrupos)) {
                Context context = getContext();
                for (CampoGrupoModel grupo : meta.camposGrupos) {
                    AppCampoGrupoLayout campoGrupoLayout = new AppCampoGrupoLayout(context);
                    campoGrupoLayout.setOrientation(LinearLayout.VERTICAL);
                    campoGrupoLayout.setGrupo(grupo);
                    mLayoutFields.addView(campoGrupoLayout);
                }
            }
            mButtonSalvar.setVisibility(View.VISIBLE);
        }
    }

    private void salvar() {
        boolean valid = validate();
        if (valid) {
            Long tipoProcessoId = TypeUtils.parse(Long.class, mSpinnerTipoProcesso.getValue());
            ProcessoModel processoModel = new ProcessoModel();
            processoModel.tipoProcesso = new TipoProcessoModel(tipoProcessoId);
            int childCount = mLayoutFields.getChildCount();
            if (childCount > 0) {
                List<CampoGrupoModel> grupoModels = new ArrayList<>();
                for (int i = 0; i < childCount; i++) {
                    View view = mLayoutFields.getChildAt(i);
                    if (view instanceof AppCampoGrupoLayout) {
                        AppCampoGrupoLayout grupoLayout = (AppCampoGrupoLayout) view;
                        CampoGrupoModel grupoModel = grupoLayout.getValue();
                        grupoModels.add(grupoModel);
                    }
                }
                processoModel.gruposCampos = grupoModels;
            }
            if (CollectionUtils.isNotEmpty(mUris)) {
                List<UploadModel> uploads = new ArrayList<>(mUris.size());
                for (Uri mUri : mUris) {
                    String path = mUri.getPath();
                    uploads.add(new UploadModel(path));
                }
                processoModel.uploads = uploads;
            }
            showProgress();
            Observable<ProcessoModel> observable = ProcessoService.Async.salvar(processoModel);
            prepare(observable).subscribe(new Subscriber<ProcessoModel>() {
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
                public void onNext(ProcessoModel processoModel) {
                    hideProgress();
                    startService(processoModel.id);
                }
            });
        }
    }

    private void startService(Long id) {
        Context context = getBaseContext();
        Intent intent = new Intent(context, DigitalizacaoService.class);
        intent.putExtra(DigitalizacaoService.KEY_PROCESSO, id);
        context.startService(intent);
    }

    private boolean validate() {
        boolean valid = true;
        Long tipoProcessoId = TypeUtils.parse(Long.class, mSpinnerTipoProcesso.getValue());
        if (tipoProcessoId == null) {
            valid = false;
            mSpinnerTextInputLayout.setErrorEnabled(true);
            mSpinnerTextInputLayout.setError(getString(R.string.msg_required_field, getString(R.string.tipo_processo)));
        } else {
            mSpinnerTextInputLayout.setError(null);
            mSpinnerTextInputLayout.setErrorEnabled(false);
        }
        int childCount = mLayoutFields.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View view = mLayoutFields.getChildAt(i);
                if (view instanceof AppCampoGrupoLayout) {
                    AppCampoGrupoLayout grupoLayout = (AppCampoGrupoLayout) view;
                    if (!grupoLayout.isValid()) {
                        valid = false;
                    }
                }
            }
            if (!valid) {
                Context context = getContext();
                Toast.makeText(context, R.string.msg_form_invalido, Toast.LENGTH_SHORT).show();
            }
        }
        if (valid && CollectionUtils.isEmpty(mUris)) {
            TipoProcessoMeta meta = mTipoProcessoDataSet.meta;
            List<TipoDocumentoModel> tiposDocumentos = meta.getTiposDocumentos(true);
            if (CollectionUtils.isNotEmpty(tiposDocumentos)) {
                valid = false;
                TipoProcessoModel data = mTipoProcessoDataSet.data;
                final String key = TipoDocumentoModel.getPrefKeyById(data.id);
                Boolean display = PreferencesUtils.get(Boolean.class, key, true);
                if (!display) {
                    FragmentActivity activity = getActivity();
                    Toast.makeText(activity, R.string.msg_required_documents, Toast.LENGTH_LONG).show();
                    openScan();
                } else {
                    DocumentoDialog dialog = DocumentoDialog.newInstance(meta.tiposDocumentos, new DocumentoDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(boolean answer, boolean displayMore) {
                            if (answer) {
                                openScan();
                            }
                            PreferencesUtils.put(key, displayMore);
                        }
                    });
                    FragmentManager fragmentManager = getFragmentManager();
                    dialog.show(fragmentManager, dialog.getClass().getSimpleName());
                }
            }
        }
        return valid;
    }
}