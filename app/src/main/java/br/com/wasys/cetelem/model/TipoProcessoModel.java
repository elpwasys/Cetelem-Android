package br.com.wasys.cetelem.model;

import br.com.wasys.library.model.Model;
import br.com.wasys.library.widget.AppSpinner;

/**
 * Created by pascke on 24/06/17.
 */

public class TipoProcessoModel extends Model implements AppSpinner.Option {

    public String nome;

    public TipoProcessoModel() {

    }

    public TipoProcessoModel(Long id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return nome;
    }

    @Override
    public String getValue() {
        return String.valueOf(id);
    }
}