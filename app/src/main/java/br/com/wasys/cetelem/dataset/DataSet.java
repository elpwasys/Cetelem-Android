package br.com.wasys.cetelem.dataset;

import java.io.Serializable;

/**
 * Created by pascke on 24/06/17.
 */

public class DataSet<Data extends Serializable, Meta extends Serializable> implements Serializable {

    public Data data;
    public Meta meta;
}