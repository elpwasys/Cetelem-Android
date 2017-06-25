package br.com.wasys.cetelem.model;

import android.support.annotation.StringRes;

import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.library.model.Model;

/**
 * Created by pascke on 24/06/17.
 */

public class ProcessoModel extends Model {

    public Status status;

    public UsuarioModel autor;
    public UsuarioModel analista;
    public TipoProcessoModel tipoProcesso;

    public List<CampoGrupoModel> gruposCampos;

    public enum Status {
        RASCUNHO (R.string.status_processo_rascunho),
        AGUARDANDO_ANALISE (R.string.status_processo_aguardando_analise),
        EM_ANALISE (R.string.status_processo_em_analise),
        PENDENTE (R.string.status_processo_pendente),
        EM_ACOMPANHAMENTO (R.string.status_processo_em_acompanhamento),
        CONCLUIDO (R.string.status_processo_concluido),
        CANCELADO (R.string.status_processo_cancelado);
        public int stringRes;
        public int drawableRes;
        Status(@StringRes int stringRes) {
            this.stringRes = stringRes;
        }
    }
}
