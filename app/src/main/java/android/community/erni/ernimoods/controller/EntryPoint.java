package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.community.erni.ernimoods.R;

import android.community.erni.ernimoods.model.Mood;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);

    }

    /*
     this creates the 'menu' which at the moment is only a preferences button rather than menu items
      */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry_point, menu);
        return true;
    }

    /*
     handles what to do when a menu item is selected
      */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_preferences) {

                // launch the SettingsActivity (preferences screen)
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);

        return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
