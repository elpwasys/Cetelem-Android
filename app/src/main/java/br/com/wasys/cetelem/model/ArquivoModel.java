package br.com.wasys.cetelem.model;

import br.com.wasys.cetelem.realm.Arquivo;

/**
 * Created by pascke on 04/07/17.
 */

public class ArquivoModel {

    public Long id;
    public String caminho;

    public static ArquivoModel from(Arquivo arquivo) {
        if (arquivo == null) {
            return null;
        }
        ArquivoModel model = new ArquivoModel();
        model.id = arquivo.id;
        model.caminho = arquivo.caminho;
        return model;
    }
}
