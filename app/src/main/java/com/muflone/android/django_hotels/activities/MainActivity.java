package com.muflone.android.django_hotels.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.muflone.android.django_hotels.Settings;
import com.muflone.android.django_hotels.R;
import com.muflone.android.django_hotels.Singleton;
import com.muflone.android.django_hotels.api.Api;
import com.muflone.android.django_hotels.database.AppDatabase;
import com.muflone.android.django_hotels.fragments.AboutFragment;
import com.muflone.android.django_hotels.fragments.ExtrasFragment;
import com.muflone.android.django_hotels.fragments.HomeFragment;
import com.muflone.android.django_hotels.fragments.ScannerFragment;
import com.muflone.android.django_hotels.fragments.SettingsFragment;
import com.muflone.android.django_hotels.fragments.StructuresFragment;
import com.muflone.android.django_hotels.fragments.SyncFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int SETTINGS_RETURN_CODE = 1;
    private Fragment fragment = null;
    private MenuItem menuItemHome = null;
    private MenuItem menuItemSettings = null;
    private MenuItem menuItemSync = null;
    private boolean backButtonPressed = false;

    // Allow the use of (insecure) drawables for API < 21
    // https://developer.android.com/reference/android/support/v7/app/AppCompatDelegate.html#setCompatVectorFromResourcesEnabled(boolean)
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // Singleton instance
        Settings settings = new Settings(this);
        Singleton.getInstance().settings = settings;
        Singleton.getInstance().api = new Api();
        // Add settings_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Add Navigation Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Find the settings MenuItems
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        for (int item = 0; item < menu.size(); item++) {
            switch (menu.getItem(item).getItemId()) {
                case R.id.menuItemHome:
                    this.menuItemHome = menu.getItem(item);
                    break;
                case R.id.menuItemSettings:
                    this.menuItemSettings = menu.getItem(item);
                    break;
                case R.id.menuItemSync:
                    this.menuItemSync = menu.getItem(item);
                    break;
            }
        }
        // Load preferences
        Log.d(TAG, "ID: " + settings.getTabletID());
        Log.d(TAG, "Key: " + settings.getTabletKey());
        if (settings.getTabletID().isEmpty() |
                settings.getTabletKey().isEmpty()) {
            String message = settings.getTabletID().isEmpty() ?
                    this.getString(R.string.message_missing_tablet_id) :
                    this.getString(R.string.message_missing_tablet_key);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            this.onNavigationItemSelected(this.menuItemSettings);
        } else {
            this.onNavigationItemSelected(this.menuItemHome);
        }
        // Reload data from database
        AppDatabase.getAppDatabase(this).reload(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save active fragment name
        if (this.fragment != null) {
            outState.putString("fragment", this.fragment.getClass().getSimpleName());
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous active fragment
        String fragmentName = savedInstanceState.getString("fragment");
        Fragment fragment = null;
        if (fragmentName != null) {
            switch (fragmentName) {
                case "HomeFragment":
                    fragment = new HomeFragment();
                    break;
                case "ScannerFragment":
                    fragment = new ScannerFragment();
                    break;
                case "StructuresFragment":
                    fragment = new StructuresFragment();
                    break;
                case "ExtrasFragment":
                    fragment = new ExtrasFragment();
                    break;
                case "SyncFragment":
                    fragment = new SyncFragment();
                    break;
                case "SettingsFragment":
                    fragment = new SettingsFragment();
                    break;
                case "AboutFragment":
                    fragment = new AboutFragment();
                    break;
                default:
                    Toast.makeText(this, fragmentName, Toast.LENGTH_SHORT).show();

            }
            if (fragment != null) {
                LoadFragment(fragment);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // Close drawer if opened
            drawer.closeDrawer(GravityCompat.START);
        } else if (! this.backButtonPressed) {
            // Show message instead of closing
            this.backButtonPressed = true;
            Toast.makeText(this, this.getString(R.string.message_press_back_again_to_exit),
                    Toast.LENGTH_SHORT).show();
            // Restore confirmation after 2 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backButtonPressed = false;
                }
            }, 2000);
        } else {
            // Accept back button to close the activity
            AppDatabase.destroyInstance();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        // Activate MenuItem
        item.setChecked(true);
        // Open Fragment or related Activity
        switch (item.getItemId()) {
            case R.id.menuItemHome:
                fragment = new HomeFragment();
                break;
            case R.id.menuItemScanner:
                fragment = new ScannerFragment();
                break;
            case R.id.menuItemStructures:
                fragment = new StructuresFragment();
                break;
            case R.id.menuItemExtras:
                fragment = new ExtrasFragment();
                break;
            case R.id.menuItemSync:
                fragment = new SyncFragment();
                break;
            case R.id.menuItemSettings:
                fragment = new SettingsFragment();
                break;
            case R.id.menuItemAbout:
                fragment = new AboutFragment();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return LoadFragment(fragment);
    }

    private boolean LoadFragment(Fragment fragment) {
        if (fragment != null) {
            // Load the selected fragment
            this.fragment = fragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, this.fragment)
                    .commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add settings_toolbar icons
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle ToolButtons clicks
        switch (item.getItemId()) {
            case R.id.toolButtonSync:
                this.onNavigationItemSelected(this.menuItemSync);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Open HomeFragment after closing the settings activity
        if (requestCode == SETTINGS_RETURN_CODE) {
            this.onNavigationItemSelected(this.menuItemHome);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
