package br.com.wasys.cetelem.model;

import android.support.annotation.StringRes;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.realm.Upload;
import br.com.wasys.library.utils.TypeUtils;
import io.realm.RealmResults;

/**
 * Created by pascke on 28/06/17.
 */

public class UploadModel {

    public Date date;
    public String path;
    public Sender sender;
    public Status status;
    public String reference;

    public UploadModel() {

    }

    public UploadModel(String path) {
        this.path = path;
    }

    public enum Sender {
        PROCESSO,
        DOCUMENTO
    }

    public enum Status {
        ERROR (R.string.erro),
        WAITING (R.string.aguardando),
        UPLOADING (R.string.enviando);
        public int stringRes;
        Status(@StringRes int stringRes) {
            this.stringRes = stringRes;
        }
    }

    public static UploadModel from(Upload upload) {
        if (upload == null) {
            return null;
        }
        UploadModel model = new UploadModel();
        model.date = upload.date;
        model.path = upload.path;
        model.reference = upload.reference;
        model.sender = TypeUtils.parse(Sender.class, upload.sender);
        model.status = TypeUtils.parse(Status.class, upload.status);
        return model;
    }

    public static List<UploadModel> from(RealmResults<Upload> uploads) {
        if (CollectionUtils.isEmpty(uploads)) {
            return null;
        }
        List<UploadModel> models = new ArrayList<>();
        for (Upload upload : uploads) {
            models.add(from(upload));
        }
        return models;
    }
}
