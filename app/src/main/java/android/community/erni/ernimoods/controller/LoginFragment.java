package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.UserBackend;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    public void onResume() {
        super.onResume();
        //load username and password from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = prefs.getString(getString(R.string.pref_username), null);
        String pwd = prefs.getString(getString(R.string.pref_password), null);
        this.username.setText(username);
        this.password.setText(pwd);
    }

    @Override
    public void onClick(View v) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pref_username", username.getText().toString());
        editor.commit();
        editor.putString("pref_password", password.getText().toString());
        editor.commit();
        //again, create an object to call the user-backend
        UserBackend getUser = new UserBackend();
        //attached the specified handlers
        getUser.setListener(((EntryPoint) getActivity()).getUserAuthCallback());
        getUser.setErrorListener(((EntryPoint) getActivity()).getUserNonauthCallback());

    }
}

