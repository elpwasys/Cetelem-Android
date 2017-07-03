package br.com.wasys.cetelem.dataset.meta;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.model.CampoGrupoModel;
import br.com.wasys.cetelem.model.TipoDocumentoModel;
import br.com.wasys.cetelem.model.TipoProcessoModel;

/**
 * Created by pascke on 24/06/17.
 */

public class TipoProcessoMeta extends Meta {

    public ArrayList<CampoGrupoModel> gruposCampos;
    public ArrayList<TipoDocumentoModel> tiposDocumentos;

    public List<TipoDocumentoModel> getTiposDocumentos(Boolean obrigatorio) {
        List<TipoDocumentoModel> itens = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tiposDocumentos)) {
            for (TipoDocumentoModel item : tiposDocumentos) {
                if (item.obrigatorio == obrigatorio) {
                    itens.add(item);
                }
            }
        }
        return itens;
    }
}