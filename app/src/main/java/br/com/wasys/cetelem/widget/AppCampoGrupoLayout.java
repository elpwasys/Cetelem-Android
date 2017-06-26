package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.model.CampoGrupoModel;
import br.com.wasys.cetelem.model.CampoModel;
import br.com.wasys.library.utils.AndroidUtils;

/**
 * Created by pascke on 25/06/17.
 */

public class AppCampoGrupoLayout extends LinearLayout {

    private CampoGrupoModel mGrupo;

    public AppCampoGrupoLayout(Context context) {
        super(context);
    }

    public AppCampoGrupoLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppCampoGrupoLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppCampoGrupoLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setGrupo(CampoGrupoModel grupo) {
        mGrupo = grupo;
        create();
    }

    private void clear() {
        removeAllViews();
    }

    private void create() {
        clear();
        createHeader();
        createFields();
    }

    private void createHeader() {

        Context context = getContext();

        TextView textView = new TextView(context);
        textView.setText(StringUtils.capitalize(mGrupo.nome));

        int margin = AndroidUtils.toPixels(context, 4f);
        int marginTop = AndroidUtils.toPixels(context, 32f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, marginTop, margin, margin);
        textView.setLayoutParams(params);

        TextViewCompat.setTextAppearance(textView, android.R.style.TextAppearance_Medium);

        int color = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);
        textView.setTextColor(color);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.bg_section, null);
        textView.setBackground(drawable);

        addView(textView);
    }

    public boolean isValid() {
        boolean valid = true;
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (view instanceof AppCampoLayout) {
                    AppCampoLayout campoLayout = (AppCampoLayout) view;
                    if (!campoLayout.validate()) {
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }

    public CampoGrupoModel getValue() {
        CampoGrupoModel grupoModel = new CampoGrupoModel();
        grupoModel.id = mGrupo.id;
        grupoModel.nome = mGrupo.nome;
        grupoModel.ordem = mGrupo.ordem;
        grupoModel.campos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mGrupo.campos)) {
            for (CampoModel campo : mGrupo.campos) {
                CampoModel campoModel = new CampoModel();
                campoModel.id = campo.id;
                campoModel.nome = campo.nome;
                View view = findViewWithTag(campo.nome);
                if (view instanceof AppCampoLayout) {
                    AppCampoLayout campoLayout = (AppCampoLayout) view;
                    campoModel.valor = campoLayout.getValue();
                }
                grupoModel.campos.add(campoModel);
            }
        }
        return grupoModel;
    }

    private void createFields() {
        if (CollectionUtils.isNotEmpty(mGrupo.campos)) {
            Context context = getContext();
            for (CampoModel campo : mGrupo.campos) {
                View view = null;
                CampoModel.Tipo tipo = campo.tipo;
                if (tipo != null) {
                    switch (tipo) {
                        case DATA:
                        case TEXTO:
                        case EMAIL:
                        case MOEDA:
                        case INTEIRO:
                        case TEXTO_LONGO:
                            AppCampoLayout campoLayout = new AppCampoLayout(context);
                            campoLayout.configurar(campo);
                            view = campoLayout;
                            break;
                    }
                }
                if (view != null) {
                    view.setTag(campo.nome);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int marginTop = AndroidUtils.toPixels(context, 16f);
                    layoutParams.setMargins(0, marginTop, 0, 0);
                    view.setLayoutParams(layoutParams);
                    addView(view);
                }
            }
        }
    }
}
