package br.com.wasys.cetelem.paging;

import java.util.List;

/**
 * Created by pascke on 09/05/16.
 */
public abstract class PagingModel<T> {

    private int page;
    private int qtde;
    private int count;
    private int pageSize;
    private List<T> records;

    public int getNextPage() {
        if (hasNext()) {
            return (page + 1);
        }
        return page;
    }

    public int getPreviousPage() {
        if (hasPrevious()) {
            return (page - 1);
        }
        return page;
    }

    public boolean hasNext() {
        return page < (qtde - 1);
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public int getPage() {
        return page;
    }

    public int getQtde() {
        return qtde;
    }

    public int getCount() {
        return count;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setQtde(int qtde) {
        this.qtde = qtde;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
