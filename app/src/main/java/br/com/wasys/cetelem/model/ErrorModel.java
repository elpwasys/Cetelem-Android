package br.com.wasys.cetelem.model;

import android.support.annotation.StringRes;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.realm.Error;
import br.com.wasys.cetelem.realm.Upload;
import br.com.wasys.library.utils.TypeUtils;
import io.realm.RealmResults;

/**
 * Created by pascke on 28/06/17.
 */

public class ErrorModel {

    public Date date;

    public String message;
    public String reference;

    public Action action;
    public Generator generator;

    public ErrorModel() {

    }

    public enum Action {
        UPLOAD
    }

    public enum Generator {
        PROCESSO (R.string.processo),
        DOCUMENTO (R.string.documento);
        public int stringRes;
        Generator(@StringRes int stringRes) {
            this.stringRes = stringRes;
        }
    }

    public static ErrorModel from(Error error) {
        if (error == null) {
            return null;
        }
        ErrorModel model = new ErrorModel();
        model.date = error.date;
        model.message = error.message;
        model.reference = error.reference;
        model.action = TypeUtils.parse(Action.class, error.action);
        model.generator = TypeUtils.parse(Generator.class, error.generator);
        return model;
    }

    public static List<ErrorModel> from(RealmResults<Error> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return null;
        }
        List<ErrorModel> models = new ArrayList<>();
        for (Error error : errors) {
            models.add(from(error));
        }
        return models;
    }
}
