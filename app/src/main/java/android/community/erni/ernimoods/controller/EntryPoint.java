package android.community.erni.ernimoods.controller;

import android.app.Activity;

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

        // call this method to redirect the user depending on if they are already signed up or not
        redirect(welcomeText);

    }

    private void redirect (View view) {
        //Add check whether user is registered here
        boolean userRegistered = false;

        Intent intent = null;

        //forward either to signup-form or set mood
        if(userRegistered){
            intent = new Intent(this, MyMoodActivity.class);
        }else{
            intent = new Intent(this, SignUpActivity.class);
        }

        startActivity(intent);
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

    public void showMyMood(View v) {
        Intent i = new Intent(this,MyMoodActivity.class);
        startActivity(i);
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
