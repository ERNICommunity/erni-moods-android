package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Settings Activity. Hosts SettingsFragment.
 * This is just boilerplate stuff.
 * Created by gus on 24.08.14.
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
        }
        return true;
    }
}
