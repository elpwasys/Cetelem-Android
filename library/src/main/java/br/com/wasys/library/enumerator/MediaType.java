package br.com.wasys.library.enumerator;

/**
 * Created by pascke on 03/08/16.
 */
public enum MediaType {
    TEXT_XML ("text/xml"),
    TEXT_HTML ("text/html"),
    IMAGE_GIF ("image/gif"),
    IMAGE_PNG ("image/png"),
    IMAGE_JPEG ("image/jpeg"),
    TEXT_PLAIN ("text/plain"),
    APPLICATION_PDF ("application/pdf"),
    APPLICATION_XML ("application/xml"),
    APPLICATION_JSON ("application/json"),
    APPLICATION_OCTET_STREAM ("application/octet-stream");
    public String value;
    MediaType(String value) {
        this.value = value;
    }
}