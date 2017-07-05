package br.com.wasys.cetelem.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.dialog.DigitalizacaoErrorDialog;
import br.com.wasys.cetelem.model.CampoGrupoModel;
import br.com.wasys.cetelem.model.DigitalizacaoModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.model.TipoProcessoModel;
import br.com.wasys.cetelem.service.DigitalizacaoService;
import br.com.wasys.cetelem.service.ProcessoService;
import br.com.wasys.cetelem.widget.AppGroupInputLayout;
import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.FragmentUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by pascke on 24/06/17.
 */

public class ProcessoEdicaoFragment extends CetelemFragment {

    @BindView(R.id.text_tipo) TextView mTipoTextView;
    @BindView(R.id.button_error) Button mErrorButton;
    @BindView(R.id.layout_fields) LinearLayout mLayoutFields;

    private Long mId;
    private ProcessoModel mProcesso;
    private DigitalizacaoModel mDigitalizacaoModel;

    private static final String KEY_ID = ProcessoEdicaoFragment.class.getName() + ".id";

    public static ProcessoEdicaoFragment newInstance(Long id) {
        ProcessoEdicaoFragment fragment = new ProcessoEdicaoFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_processo_edicao, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciar();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_processo_edicao, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_collection:
                abrirDocumentos();
                return true;
        }
        return false;
    }

    @OnClick(R.id.button_salvar)
    public void onSalvarClick() {
        startAsyncSalvar();
    }

    @OnClick(R.id.button_error)
    public void onErrorClick() {
        DigitalizacaoErrorDialog dialog = DigitalizacaoErrorDialog.newInstance(mId, DigitalizacaoModel.Tipo.TIPIFICACAO, new DigitalizacaoErrorDialog.OnUplodErrorListener() {
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

    private void iniciar() {
        if (mId != null) {
            startAsyncEdicaoById(mId);
            startAsyncDigitalizacaoBy(mId);
        }
    }

    private void abrirDocumentos() {
        DocumentoListaFragment fragment = DocumentoListaFragment.newInstance(mId);
        FragmentUtils.replace(getActivity(), R.id.content_main, fragment, fragment.getClass().getSimpleName());
    }

    private void reenviar() {
        br.com.wasys.cetelem.background.DigitalizacaoService.start(getContext(), mId);
        mErrorButton.setVisibility(View.GONE);
        Toast.makeText(getContext(), R.string.msg_processo_reenviado, Toast.LENGTH_SHORT).show();
    }

    private boolean validate() {
        boolean valid = true;
        int childCount = mLayoutFields.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View view = mLayoutFields.getChildAt(i);
                if (view instanceof AppGroupInputLayout) {
                    AppGroupInputLayout grupoLayout = (AppGroupInputLayout) view;
                    if (!grupoLayout.isValid()) {
                        valid = false;
                    }
                }
            }
            if (!valid) {
                Toast.makeText(getContext(), R.string.msg_form_invalido, Toast.LENGTH_SHORT).show();
            }
        }
        return valid;
    }

    /**
     *
     * COMPLETED METHODS ASYNCHRONOUS HANDLERS
     *
     */
    private void onAsyncSalvarCompleted() {
        Toast.makeText(getContext(), R.string.msg_processo_salvo_sucesso, Toast.LENGTH_SHORT).show();
    }

    private void onAsyncEdicaoCompleted(ProcessoModel model) {
        mProcesso = model;
        TipoProcessoModel tipoProcesso = mProcesso.tipoProcesso;
        FieldUtils.setText(mTipoTextView, tipoProcesso.nome);
        if (CollectionUtils.isNotEmpty(mProcesso.gruposCampos)) {
            Context context = getContext();
            for (CampoGrupoModel grupo : mProcesso.gruposCampos) {
                AppGroupInputLayout campoGrupoLayout = new AppGroupInputLayout(context);
                campoGrupoLayout.setOrientation(LinearLayout.VERTICAL);
                campoGrupoLayout.setGrupo(grupo);
                mLayoutFields.addView(campoGrupoLayout);
            }
        }
    }

    private void onAsyncDigitalizacaoCompleted(DigitalizacaoModel model) {
        mDigitalizacaoModel = model;
        int visibility = View.GONE;
        if (model != null) {
            if (DigitalizacaoModel.Status.ERRO.equals(mDigitalizacaoModel.status)) {
                visibility = View.VISIBLE;
            }
        }
        mErrorButton.setVisibility(visibility);
    }

    /**
     *
     * ASYNCHRONOUS METHODS
     *
     */
    private void startAsyncEdicaoById(Long id) {
        showProgress();
        Observable<ProcessoModel> observable = ProcessoService.Async.editar(id);
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
                onAsyncEdicaoCompleted(processoModel);
            }
        });
    }

    private void startAsyncDigitalizacaoBy(Long id) {
        String reference = String.valueOf(id);
        Observable<DigitalizacaoModel> observable = DigitalizacaoService.Async.getBy(reference, DigitalizacaoModel.Tipo.TIPIFICACAO, DigitalizacaoModel.Status.ERRO);
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

    private void startAsyncSalvar() {
        boolean valid = validate();
        if (valid) {
            int childCount = mLayoutFields.getChildCount();
            if (childCount > 0) {
                ArrayList<CampoGrupoModel> grupoModels = new ArrayList<>();
                for (int i = 0; i < childCount; i++) {
                    View view = mLayoutFields.getChildAt(i);
                    if (view instanceof AppGroupInputLayout) {
                        AppGroupInputLayout grupoLayout = (AppGroupInputLayout) view;
                        CampoGrupoModel grupoModel = grupoLayout.getValue();
                        grupoModels.add(grupoModel);
                    }
                }
                mProcesso.gruposCampos = grupoModels;
            }
            showProgress();
            Observable<ProcessoModel> observable = ProcessoService.Async.salvar(mProcesso);
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
                    onAsyncSalvarCompleted();
                }
            });
        }
    }
}