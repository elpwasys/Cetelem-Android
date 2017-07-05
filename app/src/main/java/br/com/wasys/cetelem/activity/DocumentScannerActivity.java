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
import android.support.v4.app.FragmentManager;
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

import br.com.wasys.cetelem.BuildConfig;
import br.com.wasys.cetelem.Permission;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.adapter.ImagePageAdapter;
import br.com.wasys.cetelem.dialog.DocumentoDialog;
import br.com.wasys.cetelem.model.ImagemModel;
import br.com.wasys.cetelem.model.TipoDocumentoModel;
import br.com.wasys.library.utils.AndroidUtils;
import br.com.wasys.library.utils.DateUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DocumentScannerActivity extends CetelemActivity implements ViewPager.OnPageChangeListener {

    private int mPosition = -1;

    private ArrayList<Uri> mUris;
    private ArrayList<Uri> mPassedUris;
    private ArrayList<Uri> mMarkedDeleteUris;
    private ArrayList<TipoDocumentoModel> mDocumentos;

    private ImagePageAdapter mImagePageAdapter;

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.indicator) CirclePageIndicator mCirclePageIndicator;

    @BindView(R.id.button_check) FloatingActionButton mCheckFloatingButton;
    @BindView(R.id.button_delete) FloatingActionButton mDeleteFloatingButton;

    private static final String KEY_URI = DocumentScannerActivity.class.getName() + ".mUris";
    private static final String KEY_DOCUMENTOS = DocumentScannerActivity.class.getName() + ".mDocumentos";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public static Intent newIntent(Context context, ArrayList<Uri> uris, ArrayList<TipoDocumentoModel> documentos) {
        Intent intent = new Intent(context, DocumentScannerActivity.class);
        Bundle extras = new Bundle();
        if (CollectionUtils.isNotEmpty(uris)) {
            extras.putParcelableArrayList(KEY_URI, uris);
        }
        if (CollectionUtils.isNotEmpty(documentos)) {
            extras.putSerializable(KEY_DOCUMENTOS, documentos);
        }
        intent.putExtras(extras);
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
        setContentView(R.layout.activity_document_scanner);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.documentos);
        actionBar.setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras.containsKey(KEY_URI)) {
            mUris = intent.getParcelableArrayListExtra(KEY_URI);
        } else {
            mUris = new ArrayList<>();
        }
        if (extras.containsKey(KEY_DOCUMENTOS)) {
            mDocumentos = (ArrayList<TipoDocumentoModel>) extras.getSerializable(KEY_DOCUMENTOS);
        }
        mPassedUris = new ArrayList<>(mUris);
        mMarkedDeleteUris = new ArrayList<>();
        mImagePageAdapter = new ImagePageAdapter(getSupportFragmentManager(), ImagemModel.from(mUris));
        int pixels = AndroidUtils.toPixels(this, 16f);
        mViewPager.setPageMargin(pixels);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mImagePageAdapter);
        mCirclePageIndicator.setViewPager(mViewPager);
        toggleButtonsVisible();
        if (CollectionUtils.isEmpty(mUris)) {
            openScanner();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_document_scan, menu);
        if (CollectionUtils.isEmpty(mDocumentos)) {
            MenuItem item = menu.findItem(R.id.action_info);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            openScanner();
            return true;
        } else if (id == R.id.action_info) {
            openInfo();
            return true;
        } else if (id == android.R.id.home) {
            onCancel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onCancel();
    }

    @OnClick(R.id.button_check)
    public void onCheckButtonClick() {
        if (CollectionUtils.isEmpty(mUris) && CollectionUtils.isEmpty(mMarkedDeleteUris)) {
            onCancel();
        } else {
            if (CollectionUtils.isNotEmpty(mMarkedDeleteUris)) {
                for (Uri uri : mMarkedDeleteUris) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
            Intent intent = getIntent();
            intent.putParcelableArrayListExtra(MediaStore.EXTRA_OUTPUT, mUris);
            setResult(RESULT_OK, intent);
            finish();
        }
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

    private void onCancel() {
        deleteFiles();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void delete(final Uri uri) {
        if (mPassedUris.contains(uri)) {
            mMarkedDeleteUris.add(uri);
        } else {
            File file = new File(uri.getPath());
            if (file.exists()) {
                file.delete();
            }
        }
        mUris.remove(uri);
        notifyDataSetChanged();
    }

    private void toggleButtonsVisible() {
        if (CollectionUtils.isEmpty(mUris)) {
            mDeleteFloatingButton.setVisibility(View.GONE);
            if (CollectionUtils.isEmpty(mMarkedDeleteUris)) {
                mCheckFloatingButton.setVisibility(View.GONE);
            }
        } else {
            mCheckFloatingButton.setVisibility(View.VISIBLE);
            mDeleteFloatingButton.setVisibility(View.VISIBLE);
        }
    }

    private void deleteFiles() {
        if (CollectionUtils.isNotEmpty(mUris)) {
            for (Uri uri : mUris) {
                if (!mPassedUris.contains(uri)) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }

    private void notifyDataSetChanged() {
        mImagePageAdapter = new ImagePageAdapter(getSupportFragmentManager(), ImagemModel.from(mUris));
        mViewPager.setAdapter(mImagePageAdapter);
        if (CollectionUtils.isNotEmpty(mUris)) {
            mViewPager.setCurrentItem(mUris.size() - 1);
        }
        mCirclePageIndicator.notifyDataSetChanged();
        toggleButtonsVisible();
    }

    private void openInfo() {
        if (CollectionUtils.isNotEmpty(mDocumentos)) {
            DocumentoDialog dialog = DocumentoDialog.newInstance(mDocumentos, new DocumentoDialog.OnDismissListener() {
                @Override
                public void onDismiss(boolean answer, boolean displayMore) { }
            }, false);
            FragmentManager manager = getSupportFragmentManager();
            dialog.show(manager,  dialog.getClass().getSimpleName());
        }
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
                    String name = DateUtils.format(date, "yyyyMMdd_HHmmssSSS.'jpg'");
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
                    handler.postDelayed(scanRunnable, 500);
                    break;
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            if (CollectionUtils.isEmpty(mUris)) {
                onCancel();
            }
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