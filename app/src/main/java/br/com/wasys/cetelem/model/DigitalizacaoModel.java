package br.com.wasys.cetelem.model;

import android.support.annotation.StringRes;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.realm.Arquivo;
import br.com.wasys.cetelem.realm.Digitalizacao;
import br.com.wasys.library.utils.TypeUtils;

/**
 * Created by pascke on 04/07/17.
 */

public class DigitalizacaoModel {

    public Long id;
    public Tipo tipo;
    public Status status;
    public String mensagem;
    public String referencia;
    public Integer tentativas;

    public Date dataHora;
    public Date dataHoraEnvio;
    public Date dataHoraRetorno;

    public List<ArquivoModel> arquivos;

    public enum Tipo {
        DOCUMENTO (R.string.documento),
        TIPIFICACAO (R.string.tipificacao);
        public int stringRes;
        Tipo(@StringRes int stringRes) {
            this.stringRes = stringRes;
        }
    }

    public enum Status {
        ERRO (R.string.erro),
        ENVIADO (R.string.enviado),
        ENVIANDO (R.string.enviando),
        AGUARDANDO (R.string.aguardando);
        public int stringRes;
        Status(@StringRes int stringRes) {
            this.stringRes = stringRes;
        }
    }

    public static DigitalizacaoModel from(Digitalizacao digitalizacao) {
        if (digitalizacao == null) {
            return null;
        }
        DigitalizacaoModel model = new DigitalizacaoModel();
        model.id = digitalizacao.id;
        model.tipo = TypeUtils.parse(DigitalizacaoModel.Tipo.class, digitalizacao.tipo);
        model.status = TypeUtils.parse(DigitalizacaoModel.Status.class, digitalizacao.status);
        model.mensagem = digitalizacao.mensagem;
        model.referencia = digitalizacao.referencia;
        model.tentativas = digitalizacao.tentativas;
        model.dataHora = digitalizacao.dataHora;
        model.dataHoraEnvio = digitalizacao.dataHoraEnvio;
        model.dataHoraRetorno = digitalizacao.dataHoraRetorno;
        if (CollectionUtils.isNotEmpty(digitalizacao.arquivos)) {
            model.arquivos = new ArrayList<>();
            for (Arquivo arquivo : digitalizacao.arquivos) {
                model.arquivos.add(ArquivoModel.from(arquivo));
            }
        }
        return model;
    }
}
