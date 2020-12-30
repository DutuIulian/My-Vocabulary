package com.example.belvito.japanesevocabulary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private DatabaseHelper databaseHelper;
    private DefinitionsManager definitionsManager;
    private HomeFragment homeFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        databaseHelper = new DatabaseHelper(this);
        definitionsManager = new DefinitionsManager(databaseHelper, this);

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            homeFragment.setDefinitionsManager(definitionsManager);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    homeFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        requestPermissions();
    }

    private void requestPermissions() {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        final String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DefinitionsFragment definitionsFragment;
        DatabaseFragment databaseFragment;

        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        homeFragment).commit();
                break;
            case R.id.nav_definitions:
                definitionsFragment = new DefinitionsFragment();
                definitionsFragment.setDatabase(databaseHelper.getReadableDatabase());
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        definitionsFragment).commit();
                break;
            case R.id.nav_database:
                databaseFragment = new DatabaseFragment();
                databaseFragment.setDatabaseHelper(databaseHelper);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        databaseFragment).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void informationChanged(String information) {
        homeFragment.informationChanged(information);
    }
}
