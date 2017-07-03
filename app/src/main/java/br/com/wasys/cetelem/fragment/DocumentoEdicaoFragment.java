package br.com.wasys.cetelem.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.DocumentoModel;
import br.com.wasys.cetelem.service.DocumentoService;
import br.com.wasys.library.utils.FieldUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocumentoEdicaoFragment extends CetelemFragment {

    @BindView(R.id.text_view_data) TextView mDataTextView;
    @BindView(R.id.text_view_nome) TextView mNomeTextView;
    @BindView(R.id.text_view_status) TextView mStatusTextView;
    @BindView(R.id.text_view_versao) TextView mVersaoTextView;
    @BindView(R.id.image_view_status) ImageView mStatusImagemView;

    private Long mId;
    private DocumentoModel mDocumento;

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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        obter();
    }

    private void obter() {
        if (mId != null) {
            obter(mId);
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
}
