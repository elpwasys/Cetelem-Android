package br.com.wasys.library.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.wasys.library.R;

/**
 * Created by pascke on 24/06/17.
 */

public class Spinner extends AppCompatAutoCompleteTextView implements AdapterView.OnItemClickListener {

    private boolean mIsPopup;
    private long mStartClickTime;
    private int mPosition = ListView.INVALID_POSITION;

    private static final int MAX_CLICK_DURATION = 200;
    
    private List<? extends Option> options;
    private OnOptionClickListener mOnOptionClickListener;

    public Spinner(Context context) {
        super(context);
        configuration(context);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        configuration(context);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configuration(context);
    }

    private void configuration(Context context) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_drop_down);
        drawable.mutate().setAlpha(128);
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
        setOnItemClickListener(this);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            ListAdapter adapter = getAdapter();
            if (adapter != null) {
                performFiltering("", 0);
            }
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getWindowToken(), 0);
            setKeyListener(null);
            dismissDropDown();
        } else {
            mIsPopup = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mStartClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            }
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - mStartClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    if (mIsPopup) {
                        dismissDropDown();
                        mIsPopup = false;
                    } else {
                        requestFocus();
                        showDropDown();
                        mIsPopup = true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mIsPopup = false;
        mPosition = position;
        if (mOnOptionClickListener != null) {
            String value = getValue();
            mOnOptionClickListener.onOptionClick(value);
        }
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom);
    }

    public String getValue() {
        String value = null;
        if (mPosition > ListView.INVALID_POSITION) {
            if (CollectionUtils.isNotEmpty(options) && mPosition < options.size()) {
                Option option = options.get(mPosition);
                value = option.getValue();
            }
        }
        return value;
    }

    public void setOnOptionClickListener(OnOptionClickListener onOptionClickListener) {
        this.mOnOptionClickListener = onOptionClickListener;
    }

    public void setOptions(List<? extends Option> options) {
        setAdapter(null);
        this.options = options;
        if (CollectionUtils.isNotEmpty(options)) {
            List<String> labels = new ArrayList<>(options.size());
            for (Option option : options) {
                labels.add(option.getLabel());
            }
            Context context = getContext();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, labels);
            setAdapter(adapter);
        }
    }

    public interface Option {
        String getLabel();
        String getValue();
    }

    public interface OnOptionClickListener {
        void onOptionClick(String value);
    }
}
