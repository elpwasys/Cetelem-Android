package br.com.wasys.cetelem.model;

import br.com.wasys.library.model.Model;

/**
 * Created by pascke on 24/06/17.
 */

public class CampoModel extends Model {

    public Tipo tipo;

    public String nome;
    public String dica;
    public String valor;
    public String opcoes;

    public Integer ordem;
    public Integer tamanhoMinimo;
    public Integer tamanhoMaximo;

    public boolean obrigatorio;

    public enum Tipo {
        CEP,
        CNPJ,
        COMBO_BOX,
        CPF,
        CPF_CNPJ,
        DATA,
        EMAIL,
        HORA,
        INTEIRO,
        MOEDA,
        PLACA,
        RADIO,
        TELEFONE,
        TEXTO,
        TEXTO_LONGO,
        UF
    }
}
