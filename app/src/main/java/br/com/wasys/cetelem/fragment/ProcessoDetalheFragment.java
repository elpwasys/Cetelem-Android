package br.com.wasys.cetelem.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.dialog.DigitalizacaoDialog;
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

import static br.com.wasys.cetelem.background.DigitalizacaoService.startDigitalizacaoService;

/**
 * Created by pascke on 24/06/17.
 */

public class ProcessoDetalheFragment extends CetelemFragment {

    @BindView(R.id.layout_fields) LinearLayout mLayoutFields;
    @BindView(R.id.text_view_id) TextView mIdTextView;
    @BindView(R.id.text_view_status) TextView mStatusTextView;
    @BindView(R.id.text_view_data) TextView mDataTextView;
    @BindView(R.id.text_view_tipo_processo) TextView mTipoTextView;
    @BindView(R.id.image_view_status) ImageView mStatusImageView;

    @BindView(R.id.button_editar) FloatingActionButton mEditarFloatingActionButton;
    @BindView(R.id.button_salvar) FloatingActionButton mSalvarFloatingActionButton;

    private Long mId;
    private ProcessoModel mProcesso;
    private DigitalizacaoModel mDigitalizacao;

    private Snackbar mSnackbar;

    private static final String KEY_ID = ProcessoDetalheFragment.class.getName() + ".id";

    public static ProcessoDetalheFragment newInstance(Long id) {
        ProcessoDetalheFragment fragment = new ProcessoDetalheFragment();
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
        View view = inflater.inflate(R.layout.fragment_processo_detalhe, container, false);
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
                openDocumentoList();
                return true;
        }
        return false;
    }

    @OnClick(R.id.button_info)
    public void onInfoClick() {
        if (mId != null) {
            startAsyncDigitalizacaoBy(mId);
        }
    }

    @OnClick(R.id.button_editar)
    public void onEditarClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.editar)
                .setMessage(R.string.msg_editar_processo)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editar();
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

    private void editar() {
        int childCount = mLayoutFields.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View view = mLayoutFields.getChildAt(i);
                if (view instanceof AppGroupInputLayout) {
                    AppGroupInputLayout grupoLayout = (AppGroupInputLayout) view;
                    grupoLayout.setEnabled(true);
                }
            }
            mEditarFloatingActionButton.setVisibility(View.GONE);
            mSalvarFloatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.button_salvar)
    public void onSalvarClick() {
        startAsyncSalvar();
    }

    private void iniciar() {
        if (mId != null) {
            startAsyncEdicaoById(mId);
        }
    }

    private void openDocumentoList() {
        DocumentoListaFragment fragment = DocumentoListaFragment.newInstance(mId);
        FragmentUtils.replace(getActivity(), R.id.content_main, fragment, fragment.getClass().getSimpleName());
    }

    private void reenviar() {
        String referencia = String.valueOf(mId);
        DigitalizacaoModel.Tipo tipo = DigitalizacaoModel.Tipo.TIPIFICACAO;
        startDigitalizacaoService(getContext(), tipo, referencia);
        Toast.makeText(getContext(), R.string.msg_processo_reenviado, Toast.LENGTH_LONG).show();
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
        FieldUtils.setText(mIdTextView, mProcesso.id);
        FieldUtils.setText(mTipoTextView, tipoProcesso.nome);
        FieldUtils.setText(mDataTextView, mProcesso.dataCriacao);
        FieldUtils.setText(mStatusTextView, getString(mProcesso.status.stringRes));

        mStatusImageView.setImageResource(mProcesso.status.drawableRes);

        if (CollectionUtils.isNotEmpty(mProcesso.gruposCampos)) {
            Context context = getContext();
            for (CampoGrupoModel grupo : mProcesso.gruposCampos) {
                AppGroupInputLayout campoGrupoLayout = new AppGroupInputLayout(context);
                campoGrupoLayout.setOrientation(LinearLayout.VERTICAL);
                campoGrupoLayout.setGrupo(grupo);
                campoGrupoLayout.setEnabled(false);
                mLayoutFields.addView(campoGrupoLayout);
            }
        }

        if (!ProcessoModel.Status.PENDENTE.equals(mProcesso.status)) {
            startAsyncCheckErrorById(mId);
        } else {
            String text = getString(R.string.msg_processo_pendente);
            Snackbar snackbar = makeSnackbar(text, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.ver, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDocumentoList();
                }
            });
            snackbar.show();
        }
    }

    private void onAsyncCheckErrorCompleted(Boolean exists) {
        if (BooleanUtils.isTrue(exists)) {
            String text = getString(R.string.msg_erro_digitalizacao);
            Snackbar snackbar = makeSnackbar(text, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.abrir, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAsyncDigitalizacaoBy(mId);
                }
            });
            snackbar.show();
        }
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
        String referencia = String.valueOf(id);
        Observable<DigitalizacaoModel> observable = DigitalizacaoService.Async.getBy(referencia, DigitalizacaoModel.Tipo.TIPIFICACAO);
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


    private void startAsyncCheckErrorById(Long id) {
        String referencia = String.valueOf(id);
        Observable<Boolean> observable = DigitalizacaoService.Async.existsBy(referencia, DigitalizacaoModel.Tipo.TIPIFICACAO, DigitalizacaoModel.Status.ERRO);
        prepare(observable).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {
            }
            @Override
            public void onError(Throwable e) {
                handle(e);
            }
            @Override
            public void onNext(Boolean exists) {
                onAsyncCheckErrorCompleted(exists);
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