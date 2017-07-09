package br.com.wasys.cetelem.model;

import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by pascke on 06/07/17.
 */

public class ProcessoRegraModel implements Serializable {

    public boolean podeEnviar;
    public boolean podeEditar;
    public boolean podeExcluir;
    public boolean podeDigitalizar;
    public boolean podeResponderPendencia;

    public Map<Pendencia, String> pendencias;

    public enum Pendencia {
        DIGITALIZACAO,
        ANALISE_DOCUMENTOS,
        PENDENCIA_DOCUMENTO,
        NAO_PENDENCIA_DOCUMENTO
    }

    public String getMensagem(Pendencia pendencia) {
        if (MapUtils.isEmpty(pendencias)) {
            return null;
        }
        return pendencias.get(pendencia);
    }
}