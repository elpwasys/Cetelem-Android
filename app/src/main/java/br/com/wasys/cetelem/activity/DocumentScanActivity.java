package br.com.wasys.cetelem.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.todobom.opennotescanner.OpenNoteScannerActivity;
import com.viewpagerindicator.CirclePageIndicator;

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

public class DocumentScanActivity extends CetelemActivity implements ViewPager.OnPageChangeListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private List<Uri> mUris;

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.indicator) CirclePageIndicator mCirclePageIndicator;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, DocumentScanActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_scan);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.documentos);
        actionBar.setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        mUris = new ArrayList<>();
        //mCirclePageIndicator.setViewPager(mViewPager);
        startOpenNoteScanner();
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
            startOpenNoteScanner();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startOpenNoteScanner() {
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
                Date date = new Date();
                String name = DateUtils.format(date, "yyyyMMdd_HHmmss");
                try {
                    File file = File.createTempFile(name, ".jpg", this.getCacheDir());
                    //file.deleteOnExit();
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
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        mUris.add(uri);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        mViewPager.setAdapter(new ImagePageAdapter(fragmentManager, mUris));
                        //mPageAdapter.add(uri);
                    }
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {

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
                    startOpenNoteScanner();
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

    }
}