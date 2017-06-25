package br.com.wasys.cetelem.model;

import java.util.List;

import br.com.wasys.library.model.Model;
import br.com.wasys.library.widget.Spinner;

/**
 * Created by pascke on 24/06/17.
 */

public class TipoProcessoModel extends Model implements Spinner.Option {

    public Long id;
    public String nome;

    @Override
    public String getLabel() {
        return nome;
    }

    @Override
    public String getValue() {
        return String.valueOf(id);
    }
}