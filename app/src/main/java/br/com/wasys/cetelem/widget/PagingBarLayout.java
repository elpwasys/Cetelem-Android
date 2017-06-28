package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.paging.PagingModel;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 14/05/16.
 */

public class PagingBarLayout extends LinearLayout {

    private Callback callback;
    private PagingModel pagingModel;

    private TextView mCenterTextView;
    private ImageView mLeftImageView;
    private ImageView mRightImageView;

    public PagingBarLayout(Context context) {
        super(context);
        construct();
    }

    public PagingBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public PagingBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    private void construct() {
        Context context = getContext();
        Resources resources = getResources();
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.color.colorPrimary);
        setPadding(16, 16, 16, 16);

        // Left
        LinearLayout leftLayout = new LinearLayout(context);
        leftLayout.setGravity(Gravity.LEFT);
        leftLayout.setOrientation(HORIZONTAL);
        leftLayout.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        mLeftImageView = new ImageView(context);
        mLeftImageView.setVisibility(INVISIBLE);
        mLeftImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    int page = 0;
                    if (pagingModel != null) {
                        page = pagingModel.getPage() - 1;
                    }
                    callback.onPreviousClick(page);
                }
            }
        });
        //mLeftImageView.setBackgroundResource(R.drawable.renault_btn_default_holo_light);
        mLeftImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mLeftImageView.setImageResource(R.drawable.ic_arrow_left_white);
        leftLayout.addView(mLeftImageView);
        addView(leftLayout);
        // Center
        mCenterTextView = new TextView(context);

        mCenterTextView.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null));
        mCenterTextView.setTypeface(null, Typeface.BOLD);
        LayoutParams centerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centerLayoutParams.gravity = Gravity.CENTER;
        addView(mCenterTextView);
        // Right
        LinearLayout rightLayout = new LinearLayout(context);
        rightLayout.setGravity(Gravity.RIGHT);
        rightLayout.setOrientation(HORIZONTAL);
        rightLayout.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        mRightImageView = new ImageView(context);
        mRightImageView.setVisibility(INVISIBLE);
        mRightImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    int page = 0;
                    if (pagingModel != null) {
                        page = pagingModel.getPage() + 1;
                    }
                    callback.onNextClick(page);
                }
            }
        });
        //mRightImageView.setBackgroundResource(R.drawable.renault_btn_default_holo_light);
        mRightImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRightImageView.setImageResource(R.drawable.ic_arrow_right_white);
        rightLayout.addView(mRightImageView);
        addView(rightLayout);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setPagingModel(PagingModel pagingModel) {
        this.pagingModel = pagingModel;
        if (pagingModel.hasNext()) {
            mRightImageView.setVisibility(VISIBLE);
        }
        else {
            mRightImageView.setVisibility(INVISIBLE);
        }
        if (pagingModel.hasPrevious()) {
            mLeftImageView.setVisibility(VISIBLE);
        }
        else {
            mLeftImageView.setVisibility(INVISIBLE);
        }
        int qtde = pagingModel.getQtde();
        if (qtde > 0) {
            FieldUtils.setText(mCenterTextView, (pagingModel.getPage() + 1) + " / " + qtde);
        }
        else {
            Context context = getContext();
            FieldUtils.setText(mCenterTextView, context.getString(R.string.sem_registros_exibir));
        }
    }

    public interface Callback {
        void onNextClick(int page);
        void onPreviousClick(int page);
    }
}