package br.com.wasys.cetelem.realm;

import java.util.Date;

import br.com.wasys.cetelem.model.ErrorModel;
import br.com.wasys.cetelem.model.UploadModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by pascke on 28/06/17.
 */
public class Error extends RealmObject {

    public Date date;
    public String action;
    public String message;
    public String generator;
    public String reference;

    public void copy(ErrorModel model) {
        this.date = model.date;
        this.message = model.message;
        this.reference = model.reference;
        this.action = model.action.name();
        this.generator = model.generator.name();
        if (model.date == null) {
            this.date = new Date();
        } else {
            this.date = model.date;
        }
    }
}
