package br.com.wasys.cetelem.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.ProcessoAdapter;
import br.com.wasys.cetelem.model.PesquisaModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.paging.PagingModel;
import br.com.wasys.cetelem.service.ProcessoService;
import br.com.wasys.cetelem.widget.PagingBarLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessoPesquisaFragment extends CetelemFragment {

    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.paging_bar) PagingBarLayout mPagingBarLayout;

    private PesquisaModel mPesquisaModel;
    private PagingModel<ProcessoModel> mPagingModel;

    public static ProcessoPesquisaFragment newInstance() {
        ProcessoPesquisaFragment fragment = new ProcessoPesquisaFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_processo_pesquisa, container, false);
        setTitle(R.string.titulo_pesquisa);
        ButterKnife.bind(this, view);
        mPesquisaModel = new PesquisaModel();
        mPesquisaModel.page = 0;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_processo_pesquisa, menu);
    }

    private void atualizar() {
        List<ProcessoModel> records = mPagingModel.getRecords();
        mListView.setAdapter(new ProcessoAdapter(getBaseContext(), records));
    }

    private void pesquisar() {
        showProgress();
        Observable<PagingModel<ProcessoModel>> observable = ProcessoService.Async.pesquisar(mPesquisaModel);
        prepare(observable).subscribe(new Subscriber<PagingModel<ProcessoModel>>() {
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
            public void onNext(PagingModel<ProcessoModel> pagingModel) {
                hideProgress();
                mPagingModel = pagingModel;
            }
        });
    }
}
