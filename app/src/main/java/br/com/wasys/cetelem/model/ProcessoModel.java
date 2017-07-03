package br.com.wasys.cetelem.model;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Date;

import br.com.wasys.cetelem.Application;
import br.com.wasys.cetelem.R;
import br.com.wasys.library.model.Model;
import br.com.wasys.library.widget.AppSpinner;

/**
 * Created by pascke on 24/06/17.
 */

public class ProcessoModel extends Model {

    public Status status;
    public Date dataCriacao;
    public TipoProcessoModel tipoProcesso;

    public ArrayList<UploadModel> uploads;
    public ArrayList<CampoGrupoModel> gruposCampos;

    public enum Status implements AppSpinner.Option {
        RASCUNHO (R.string.processo_status_rascunho, R.drawable.processo_status_rascunho),
        //AGUARDANDO_ANALISE (R.string.status_processo_aguardando_analise, R.drawable.processo_status_aguardando_analise),
        EM_ANALISE (R.string.processo_status_em_analise, R.drawable.processo_status_em_analise),
        PENDENTE (R.string.processo_status_pendente, R.drawable.processo_status_pendente),
        //EM_ACOMPANHAMENTO (R.string.status_processo_em_acompanhamento, R.drawable.processo_status_em_acompanhamento),
        CONCLUIDO (R.string.processo_status_concluido, R.drawable.processo_status_concluido),
        CANCELADO (R.string.processo_status_cancelado, R.drawable.processo_status_cancelado),
        CONCLUIDO_AUTOMATICO (R.string.status_processo_concluido_automatico, R.drawable.processo_status_concluido_automatico);
        public int stringRes;
        public int drawableRes;
        Status(@StringRes int stringRes, @DrawableRes int drawableRes) {
            this.stringRes = stringRes;
            this.drawableRes = drawableRes;
        }
        @Override
        public String getValue() {
            return name();
        }
        @Override
        public String getLabel() {
            Context context = Application.getContext();
            return context.getString(stringRes);
        }
    }
}
