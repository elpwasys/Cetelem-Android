package br.com.wasys.cetelem.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.library.model.Model;

/**
 * Created by pascke on 24/06/17.
 */

public class ProcessoModel extends Model {

    public Status status;
    public Date dataCriacao;
    public TipoProcessoModel tipoProcesso;

    public List<UploadModel> uploads;
    public List<CampoGrupoModel> gruposCampos;

    public enum Status {
        RASCUNHO (R.string.status_processo_rascunho, R.drawable.processo_status_rascunho),
        AGUARDANDO_ANALISE (R.string.status_processo_aguardando_analise, R.drawable.processo_status_aguardando_analise),
        EM_ANALISE (R.string.status_processo_em_analise, R.drawable.processo_status_em_analise),
        PENDENTE (R.string.status_processo_pendente, R.drawable.processo_status_pendente),
        EM_ACOMPANHAMENTO (R.string.status_processo_em_acompanhamento, R.drawable.processo_status_em_acompanhamento),
        CONCLUIDO (R.string.status_processo_concluido, R.drawable.processo_status_concluido),
        CANCELADO (R.string.status_processo_cancelado, R.drawable.processo_status_cancelado);
        public int stringRes;
        public int drawableRes;
        Status(@StringRes int stringRes, @DrawableRes int drawableRes) {
            this.stringRes = stringRes;
            this.drawableRes = drawableRes;
        }
    }
}
