package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.app.FragmentManager;
import android.community.erni.ernimoods.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by gus on 26/10/14.
 */
public class LoginFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "LoginFragment";
    private EditText username;
    private EditText password;
    private Button loginBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        username = (EditText) view.findViewById(R.id.username_loginform);
        password = (EditText) view.findViewById(R.id.password_loginform);
        loginBtn = (Button) view.findViewById(R.id.login_loginform);
        loginBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        boolean isAuthenticated = false; // flag will be set to true if the user can be authenticated

        //TODO
        // when the user clicks on the login, check with the backend if the username exists and password is valid

        // if yes, set the isAuthenticated flag to true

        if (isAuthenticated) {
            // write the username and password to shared preferences and redirect to the moodsNearMe
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("pref_username", username.getText().toString());
            editor.commit();
            editor.putString("pref_password", password.getText().toString());
            editor.commit();

            // redirect to the MyMood by replacing the Login fragment with MyMoodFragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new MyMoodFragment()).commit();
            Log.d(TAG, "Replaced fragment with MyMood");
        }
        else {

            // if no, display an error with "try again"
        }

    }
}

