package br.com.wasys.cetelem.realm;

import android.support.annotation.StringRes;

import java.util.Date;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.UploadModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by pascke on 28/06/17.
 */
public class Upload extends RealmObject {

    @PrimaryKey
    public String path;

    public Date date;
    public String sender;
    public String status;
    public String reference;

    public void copy(UploadModel model) {
        this.sender = model.sender.name();
        this.status = model.status.name();
        this.reference = model.reference;
        if (model.date == null) {
            this.date = new Date();
        } else {
            this.date = model.date;
        }
    }
}
