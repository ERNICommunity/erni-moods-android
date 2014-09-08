package android.community.erni.ernimoods.controller;

import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.community.erni.ernimoods.R;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This is the starting Activity for the application.
 */
public class EntryPoint extends Activity {

    TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);

        // depending on whether the user is registered, inflate the relevant fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        boolean userRegistered = ((MoodsApp)getApplication()).userRegistered;
        if (userRegistered) {

            fragmentTransaction
                    .replace(R.id.fragmentContainer, new MyMoodFragment())
                    .commit();
        } else {
            fragmentTransaction
                    .replace(R.id.fragmentContainer, new SignUpFragment())
                    .commit();
        }

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



    // check network connection
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    
}
