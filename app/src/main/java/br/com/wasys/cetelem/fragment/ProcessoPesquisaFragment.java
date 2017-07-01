package br.com.wasys.cetelem.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.ProcessoAdapter;
import br.com.wasys.cetelem.model.PesquisaModel;
import br.com.wasys.cetelem.model.ProcessoModel;
import br.com.wasys.cetelem.paging.ProcessoPagingModel;
import br.com.wasys.cetelem.service.ProcessoService;
import br.com.wasys.cetelem.widget.PagingBarLayout;
import br.com.wasys.library.adapter.ListAdapter;
import br.com.wasys.library.utils.FragmentUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessoPesquisaFragment extends CetelemFragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.paging_bar) PagingBarLayout mPagingBarLayout;

    private PesquisaModel mPesquisaModel;
    private ProcessoPagingModel mPagingModel;
    private ListAdapter<ProcessoModel> mListAdapter;

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
        mListAdapter = new ProcessoAdapter(getBaseContext(), null);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mListAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPesquisaModel = new PesquisaModel();
        mPesquisaModel.page = 0;
        pesquisar();
        mPagingBarLayout.setCallback(new PagingBarLayout.Callback() {
            @Override
            public void onNextClick(int page) {
                mPesquisaModel.page = page;
                pesquisar();
            }
            @Override
            public void onPreviousClick(int page) {
                mPesquisaModel.page = page;
                pesquisar();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_processo_pesquisa, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                pesquisar();
                return true;
            case R.id.action_search:
                // ABRIR DOCUMENTOS
                return true;
        }
        return false;
    }

    private void atualizar() {
        List<ProcessoModel> records = mPagingModel.getRecords();
        mListAdapter.setRows(records);
        mPagingBarLayout.setPagingModel(mPagingModel);
    }

    private void pesquisar() {
        showProgress();
        Observable<ProcessoPagingModel> observable = ProcessoService.Async.pesquisar(mPesquisaModel);
        prepare(observable).subscribe(new Subscriber<ProcessoPagingModel>() {
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
            public void onNext(ProcessoPagingModel pagingModel) {
                hideProgress();
                mPagingModel = pagingModel;
                atualizar();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter adapter = parent.getAdapter();
        ProcessoModel processo = (ProcessoModel) adapter.getItem(position);
        ProcessoCadastroFragment fragment = ProcessoCadastroFragment.newInstance(processo.id);
        String backStackName = ProcessoCadastroFragment.class.getSimpleName();
        FragmentUtils.replace(getActivity(), R.id.content_main, fragment, backStackName);
    }
}