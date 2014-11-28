package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Fragment that provides login functionality. The user can edit hist stored credentials
 * and submit the for authorization. Optionally he can navigate to the sign-up form.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";
    private EditText username;
    private EditText password;
    private Button loginBtn;
    private TextView clickHere;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //make this fragment restorable after configuration changes
        setRetainInstance(true);

        getActivity().getActionBar().hide();

        //get handles on all the view-objects
        username = (EditText) view.findViewById(R.id.username_loginform);
        password = (EditText) view.findViewById(R.id.password_loginform);
        loginBtn = (Button) view.findViewById(R.id.login_loginform);
        clickHere = (TextView) view.findViewById(R.id.textView2);
        loginBtn.setOnClickListener(this);
        loginBtn.setClickable(true);
        clickHere.setOnClickListener(this);
        clickHere.setClickable(true);

        return view;
    }

    /**
     * Whenever the fragment resumes, prefill the text-boxes with the user-credentials
     * from the stored preferences
     */
    @Override
    public void onResume() {
        super.onResume();
        //load username and password from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = prefs.getString(getString(R.string.pref_username), null);
        String pwd = prefs.getString(getString(R.string.pref_password), null);
        this.username.setText(username);
        this.password.setText(pwd);
    }

    /**
     * Handle a click on either the login button or the link to the sign-up form
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        // forward to sign-up page
        if (v.getId() == R.id.textView2) {
            ((EntryPoint) getActivity()).swapLoginSignUp();
            //or store new credentials and initiate authorization
        } else {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("pref_username", username.getText().toString());
            editor.commit();
            editor.putString("pref_password", password.getText().toString());
            editor.commit();

            ((EntryPoint) getActivity()).authorizeUser();
        }

    }
}

