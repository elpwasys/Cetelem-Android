package br.com.wasys.cetelem.model;

import br.com.wasys.library.model.Model;

/**
 * Created by pascke on 26/06/17.
 */

public class TipoDocumentoModel extends Model {

    public String nome;
    public Integer ordem;
    public Boolean obrigatorio;

    private static final String PREF_KEY_DISPLAY_DOCUMENTS = TipoDocumentoModel.class.getSimpleName();

    public static String getPrefKeyById(Long id) {
        return PREF_KEY_DISPLAY_DOCUMENTS + "." + id;
    }
}
