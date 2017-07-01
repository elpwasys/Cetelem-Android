package br.com.wasys.cetelem.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.paging.PagingModel;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.FieldUtils;

/**
 * Created by pascke on 14/05/16.
 */

public class PagingBarLayout extends LinearLayout {

    private Callback callback;
    private PagingModel pagingModel;

    private TextView mCenterTextView;
    private ImageButton mLeftImageView;
    private ImageButton mRightImageView;

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
        // Left
        mLeftImageView = new ImageButton(context, null, android.R.attr.buttonBarButtonStyle);
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
        LayoutParams leftImageViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftImageViewLayoutParams.gravity = Gravity.CENTER;
        mLeftImageView.setLayoutParams(leftImageViewLayoutParams);
        mLeftImageView.setImageResource(R.drawable.ic_arrow_left);
        mLeftImageView.setImageTintList(ContextCompat.getColorStateList(context, android.R.color.white));
        addView(mLeftImageView);
        // Center
        mCenterTextView = new TextView(context);
        LayoutParams centerTextViewLayoutParams = new LayoutParams(1, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        centerTextViewLayoutParams.gravity = Gravity.CENTER;
        mCenterTextView.setGravity(Gravity.CENTER);
        mCenterTextView.setLayoutParams(centerTextViewLayoutParams);
        TextViewCompat.setTextAppearance(mCenterTextView, android.R.style.TextAppearance_Small);
        mCenterTextView.setTypeface(null, Typeface.BOLD);
        mCenterTextView.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null));
        addView(mCenterTextView);
        // Right
        mRightImageView = new ImageButton(context, null, android.R.attr.buttonBarButtonStyle);
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
        LayoutParams rightImageViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightImageViewLayoutParams.gravity = Gravity.CENTER;
        mRightImageView.setLayoutParams(rightImageViewLayoutParams);
        mRightImageView.setImageResource(R.drawable.ic_arrow_right);
        mRightImageView.setImageTintList(ContextCompat.getColorStateList(context, android.R.color.white));
        addView(mRightImageView);
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