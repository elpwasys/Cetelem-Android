package br.com.wasys.cetelem;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.model.UsuarioModel;
import br.com.wasys.library.utils.PreferencesUtils;

/**
 * Created by pascke on 24/06/17.
 */

public class Usuario {

    private Long id;
    private String nome;
    private String email;

    private static final String KEY_ID = Usuario.class.getName() + ".id";
    private static final String KEY_NOME = Usuario.class.getName() + ".nome";
    private static final String KEY_EMAIL = Usuario.class.getName() + ".email";

    private Usuario() {
    }


    public static Usuario from(UsuarioModel model) {
        clear();
        Usuario usuario = new Usuario();
        usuario.id = model.id;
        usuario.nome = model.nome;
        usuario.email = model.email;
        PreferencesUtils.put(KEY_ID, usuario.id);
        PreferencesUtils.put(KEY_NOME, usuario.nome);
        PreferencesUtils.put(KEY_EMAIL, usuario.email);
        return usuario;
    }

    public static void clear() {
        PreferencesUtils.remove(KEY_ID);
        PreferencesUtils.remove(KEY_NOME);
        PreferencesUtils.remove(KEY_EMAIL);
    }

    public static Usuario current() {
        Usuario usuario = null;
        Long id = PreferencesUtils.get(Long.class, KEY_ID);
        String nome = PreferencesUtils.get(String.class, KEY_NOME);
        String email = PreferencesUtils.get(String.class, KEY_EMAIL);
        if (id != null && StringUtils.isNotBlank(nome) && StringUtils.isNotBlank(email)) {
            usuario = new Usuario();
            usuario.id = id;
            usuario.nome = nome;
            usuario.email = email;
        }
        return usuario;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
