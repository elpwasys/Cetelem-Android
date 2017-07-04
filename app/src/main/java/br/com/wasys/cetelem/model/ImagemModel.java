package br.com.wasys.cetelem.model;

import android.net.Uri;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.wasys.library.model.Model;

/**
 * Created by pascke on 03/07/17.
 */

public class ImagemModel extends Model {

    public String path;

    public String nome;
    public String caminho;
    public Integer versao;
    public Boolean cache;

    public static ImagemModel from(Uri uri) {
        if (uri == null) {
            return null;
        }
        ImagemModel model = new ImagemModel();
        model.path = uri.getPath();
        return model;
    }

    public static List<ImagemModel> from(List<Uri> uris) {
        if (CollectionUtils.isEmpty(uris)) {
            return null;
        }
        List<ImagemModel> models = new ArrayList<>(uris.size());
        for (Uri uri : uris) {
            models.add(from(uri));
        }
        return models;
    }
}
