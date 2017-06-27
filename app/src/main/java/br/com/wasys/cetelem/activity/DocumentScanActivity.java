package br.com.wasys.cetelem.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.todobom.opennotescanner.OpenNoteScannerActivity;
import com.viewpagerindicator.CirclePageIndicator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.Permission;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.ImagePageAdapter;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.DateUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DocumentScanActivity extends CetelemActivity implements ViewPager.OnPageChangeListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private int mPosition = -1;
    private List<Uri> mUris;
    private ImagePageAdapter mImagePageAdapter;

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.indicator) CirclePageIndicator mCirclePageIndicator;
    @BindView(R.id.button_delete) FloatingActionButton mDeleteFloatingButton;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, DocumentScanActivity.class);
        return intent;
    }

    private Handler handler = new Handler();
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            openScanner();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_scan);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.documentos);
        actionBar.setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        mUris = new ArrayList<>();
        mImagePageAdapter = new ImagePageAdapter(getSupportFragmentManager(), mUris);
        int pixels = AndroidUtils.toPixels(this, 16f);
        mViewPager.setPageMargin(pixels);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mImagePageAdapter);
        mCirclePageIndicator.setViewPager(mViewPager);
        openScanner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_document_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            openScanner();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteFiles();
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.button_delete)
    public void onDeleteButtonClick() {
        if (CollectionUtils.isNotEmpty(mUris)) {
            if (mPosition < mUris.size()) {
                Uri uri = mUris.get(mPosition);
                delete(uri);
            }
        }
    }

    private void delete(final Uri uri) {
        Observable<Void> observable = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    file.delete();
                }
                subscriber.onCompleted();
            }
        });
        showProgress();
        prepare(observable).subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                hideProgress();
                mUris.remove(mPosition);
                notifyDataSetChanged();
                toggleDeleButtonVisible();
            }
            @Override
            public void onNext(Object o) {

            }
            @Override
            public void onError(Throwable e) {
                hideProgress();
                handle(e);
            }
        });
    }

    private void toggleDeleButtonVisible() {
        if (CollectionUtils.isEmpty(mUris)) {
            mDeleteFloatingButton.setVisibility(View.GONE);
        } else {
            mDeleteFloatingButton.setVisibility(View.VISIBLE);
        }
    }

    private void deleteFiles() {
        if (CollectionUtils.isNotEmpty(mUris)) {
            for (Uri uri : mUris) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void notifyDataSetChanged() {
        mImagePageAdapter = new ImagePageAdapter(getSupportFragmentManager(), mUris);
        mViewPager.setAdapter(mImagePageAdapter);
        if (CollectionUtils.isNotEmpty(mUris)) {
            mViewPager.setCurrentItem(mUris.size() - 1);
        }
        mCirclePageIndicator.notifyDataSetChanged();
    }

    private void openScanner() {
        boolean granted = true;
        Context context = getBaseContext();
        String[] permitions = Permission.merge(Permission.STORAGE, Manifest.permission.CAMERA);
        for (String permission : permitions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }
        if (granted) {
            if (AndroidUtils.isExternalStorageWritable()) {
                String applicationId = BuildConfig.APPLICATION_ID;
                File storage = new File(Environment.getExternalStorageDirectory(), applicationId);
                if (!storage.exists()) {
                    storage.mkdirs();
                }
                try {
                    Date date = new Date();
                    String name = DateUtils.format(date, "yyyyMMdd_HHmmss.'jpg'");
                    String path = storage.getAbsolutePath() + File.separator + name;
                    File file = new File(path);
                    file.createNewFile();
                    Uri uri = Uri.fromFile(file);
                    Intent intent = new Intent(context, OpenNoteScannerActivity.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permitions, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                    mUris.add(uri);
                    notifyDataSetChanged();
                    toggleDeleButtonVisible();
                    handler.postDelayed(scanRunnable, 500);
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            // nothing for now
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        if (ArrayUtils.isNotEmpty(grantResults)) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    granted = false;
                    break;
                }
            }
        }
        if (granted) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    openScanner();
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPosition = position;
    }
}