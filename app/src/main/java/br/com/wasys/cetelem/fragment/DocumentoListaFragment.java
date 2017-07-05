package br.com.wasys.cetelem.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.activity.DocumentScannerActivity;
import br.com.wasys.cetelem.adapter.DocumentoListAdapter;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.service.DocumentoService;
import br.com.wasys.library.utils.FragmentUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentoListaFragment extends CetelemFragment implements ExpandableListView.OnChildClickListener {

    @BindView(R.id.list_view) ExpandableListView mListView;

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
        listar();
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
                listar();
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
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            deleteFiles();
        }
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
    private void popular(List<DocumentoModel> models) {
        List<DocumentoModel> opcionais = new ArrayList<>();
        List<DocumentoModel> obrigatorios = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(models)) {
            for (DocumentoModel model : models) {
                if (BooleanUtils.isTrue(model.obrigatorio)) {
                    obrigatorios.add(model);
                } else {
                    opcionais.add(model);
                }
            }
        }
        mGroups = new ArrayList<>();
        mGroups.add(new DocumentoListAdapter.Group(R.string.obrigatorios, obrigatorios));
        mGroups.add(new DocumentoListAdapter.Group(R.string.opcionais, opcionais));
        mListView.setAdapter(new DocumentoListAdapter(getContext(), mGroups));
        for (int i = 0; i < mGroups.size(); i++) {
            mListView.expandGroup(i);
        }
    }

    // BUSCA A LISTA DE DOCUMENTOS NO SERVIDOR
    private void listar() {
        if (mId != null) {
            showProgress();
            Observable<List<DocumentoModel>> observable = DocumentoService.Async.listar(mId);
            prepare(observable).subscribe(new Subscriber<List<DocumentoModel>>() {
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
                public void onNext(List<DocumentoModel> models) {
                    hideProgress();
                    popular(models);
                }
            });
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
}