package com.example.belvito.japanesevocabulary;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.belvito.japanesevocabulary.ui.DefinitionsFragment;
import com.example.belvito.japanesevocabulary.ui.HomeFragment;

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
            homeFragment.setDatabaseHelper(databaseHelper);
            homeFragment.setDefinitionsManager(definitionsManager);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    homeFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                homeFragment = new HomeFragment();
                homeFragment.setDatabaseHelper(databaseHelper);
                homeFragment.setDefinitionsManager(definitionsManager);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        homeFragment).commit();
                break;
            case R.id.nav_definitions:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DefinitionsFragment()).commit();
                homeFragment = null;
                break;
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
