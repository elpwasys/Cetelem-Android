package br.com.wasys.cetelem.realm;

import android.content.Intent;

import java.util.Date;

import br.com.wasys.cetelem.model.DigitalizacaoModel;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by pascke on 04/07/17.
 */

public class Digitalizacao extends RealmObject {

    @PrimaryKey
    public Long id;

    @Required
    public String tipo;

    @Required
    public String status;

    public String mensagem;

    @Required
    public String referencia;

    @Required
    public Integer tentativas;

    @Required
    public Date dataHora;

    public Date dataHoraEnvio;

    public Date dataHoraRetorno;

    public RealmList<Arquivo> arquivos;

    public void populate(DigitalizacaoModel model) {
        this.tipo = model.tipo.name();
        this.status = model.status.name();
        this.mensagem = model.mensagem;
        this.referencia = model.referencia;
        this.tentativas = model.tentativas;
        this.dataHora = model.dataHora;
        this.dataHoraEnvio = model.dataHoraEnvio;
        this.dataHoraRetorno = model.dataHoraRetorno;
    }
}
