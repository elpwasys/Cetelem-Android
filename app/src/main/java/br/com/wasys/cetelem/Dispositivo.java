package br.com.wasys.cetelem;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.cetelem.model.DispositivoModel;
import br.com.wasys.cetelem.model.UsuarioModel;
import br.com.wasys.library.utils.PreferencesUtils;

/**
 * Created by pascke on 23/06/17.
 */

public class Dispositivo {

    private Long id;
    private String token;
    private String pushToken;

    private Usuario usuario;

    private static final String KEY_ID = Dispositivo.class.getName() + ".id";
    private static final String KEY_TOKEN = Dispositivo.class.getName() + ".token";
    private static final String KEY_PUSH_TOKEN = Dispositivo.class.getName() + ".pushToken";

    private Dispositivo() {

    }

    public static Dispositivo from(DispositivoModel model) {
        clear();
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.id = model.id;
        dispositivo.token = model.token;
        dispositivo.pushToken = model.pushToken;
        dispositivo.usuario = Usuario.from(model.usuario);
        PreferencesUtils.put(KEY_ID, dispositivo.id);
        PreferencesUtils.put(KEY_TOKEN, dispositivo.token);
        if (StringUtils.isNotBlank(dispositivo.pushToken)) {
            PreferencesUtils.put(KEY_PUSH_TOKEN, dispositivo.pushToken);
        }
        return dispositivo;
    }

    public static void clear() {
        PreferencesUtils.remove(KEY_ID);
        PreferencesUtils.remove(KEY_TOKEN);
        PreferencesUtils.remove(KEY_PUSH_TOKEN);
        Usuario.clear();
    }

    public static Dispositivo current() {
        Dispositivo dispositivo = null;
        Long id = PreferencesUtils.get(Long.class, KEY_ID);
        String token = PreferencesUtils.get(String.class, KEY_TOKEN);
        String pushToken = PreferencesUtils.get(String.class, KEY_PUSH_TOKEN);
        if (id != null && StringUtils.isNotBlank(token)) {
            dispositivo = new Dispositivo();
            dispositivo.id = id;
            dispositivo.token = token;
            dispositivo.pushToken = pushToken;
            dispositivo.usuario = Usuario.current();
        }
        return dispositivo;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getPushToken() {
        return pushToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
        PreferencesUtils.put(KEY_PUSH_TOKEN, pushToken);
    }
}
