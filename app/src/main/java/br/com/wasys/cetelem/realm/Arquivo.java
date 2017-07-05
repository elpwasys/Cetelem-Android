package br.com.wasys.cetelem.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by pascke on 04/07/17.
 */

public class Arquivo extends RealmObject {

    @PrimaryKey
    public Long id;

    @Required
    public String caminho;

    public Digitalizacao digitalizacao;
}
