package br.com.wasys.cetelem.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import br.com.wasys.cetelem.Dispositivo;
import br.com.wasys.cetelem.R;
import br.com.wasys.cetelem.Usuario;
import br.com.wasys.cetelem.fragment.HomeFragment;
import br.com.wasys.cetelem.fragment.ProcessoNovoFragment;
import br.com.wasys.cetelem.fragment.ProcessoPesquisaFragment;
import br.com.wasys.library.utils.FieldUtils;
import br.com.wasys.library.utils.FragmentUtils;
import butterknife.ButterKnife;

public class MainActivity extends CetelemActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Usuario usuario = Usuario.current();
        if (usuario != null) {
            View headerView = navigationView.getHeaderView(0);
            // USUARIO NOME
            TextView nomeTextView = ButterKnife.findById(headerView, R.id.usuario_nome);
            String nome = usuario.getNome();
            FieldUtils.setText(nomeTextView, nome);
            // USUARIO EMAIL
            String email = usuario.getEmail();
            TextView emailTextView = ButterKnife.findById(headerView, R.id.usuario_email);
            FieldUtils.setText(emailTextView, email);
        }

        //drawer.openDrawer(GravityCompat.START);

        HomeFragment fragment = HomeFragment.newInstance();
        FragmentUtils.replace(this, R.id.content_main, fragment, fragment.getClass().getSimpleName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() < 2) {
                drawer.openDrawer(GravityCompat.START);
            }
            else {
                super.onBackPressed();
            }
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_plus) {
            FragmentUtils.popAllBackStackImmediate(this);
            ProcessoNovoFragment fragment = ProcessoNovoFragment.newInstance();
            FragmentUtils.replace(this, R.id.content_main, fragment, fragment.getClass().getSimpleName());
        } else if (id == R.id.nav_search) {
            FragmentUtils.popAllBackStackImmediate(this);
            ProcessoPesquisaFragment fragment = ProcessoPesquisaFragment.newInstance();
            FragmentUtils.replace(this, R.id.content_main, fragment, fragment.getClass().getSimpleName());
        } else if (id == R.id.nav_power) {
            sair();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sair() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sair);
        builder.setMessage(R.string.msg_sair_conta);
        builder.setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dispositivo.clear();
                Intent intent = SplashActivity.newIntent(MainActivity.this);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
