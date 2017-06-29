package br.com.wasys.cetelem.realm;

import android.support.annotation.StringRes;

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
    public String status;
    public String reference;

    public void copy(UploadModel model) {
        //this.path = model.path;
        this.status = model.status.name();
        this.reference = model.reference;
    }
}
