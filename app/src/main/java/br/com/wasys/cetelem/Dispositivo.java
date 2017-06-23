package br.com.wasys.cetelem;

import org.apache.commons.lang3.StringUtils;

import br.com.wasys.library.utils.PreferencesUtils;

/**
 * Created by pascke on 23/06/17.
 */

public class Dispositivo {

    private Long id;
    private String token;
    private String pushToken;

    private static final String KEY_ID = Dispositivo.class.getName() + ".id";
    private static final String KEY_TOKEN = Dispositivo.class.getName() + ".token";
    private static final String KEY_PUSH_TOKEN = Dispositivo.class.getName() + ".pushToken";

    private Dispositivo() {

    }

    public Dispositivo(Long id, String token) {
        clear();
        this.id = id;
        this.token = token;
        PreferencesUtils.put(KEY_ID, id);
        PreferencesUtils.put(KEY_TOKEN, token);
    }

    public static void clear() {
        PreferencesUtils.remove(KEY_ID);
        PreferencesUtils.remove(KEY_TOKEN);
        PreferencesUtils.remove(KEY_PUSH_TOKEN);
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
        }
        return dispositivo;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
        PreferencesUtils.put(KEY_PUSH_TOKEN, pushToken);
    }
}
