package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.service.FormValidator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** Fragment to handle user registration
 * Created by gus on 24.08.14.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "SignUpFragment";

    EditText user;
    EditText pwd;
    EditText email;
    EditText phone;
    Button submit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        user = (EditText) view.findViewById(R.id.signUpUserInput);
        pwd = (EditText) view.findViewById(R.id.signUpPasswordInput);
        email = (EditText) view.findViewById(R.id.signUpEmailInput);
        phone = (EditText) view.findViewById(R.id.signUpPhoneInput);

        // attach on click listener to submit button
        submit = (Button) view.findViewById(R.id.signUpButton);
        submit.setOnClickListener(this);

        //Attach validators for eMail
        EditText email = (EditText) view.findViewById(R.id.signUpEmailInput);
        //This listener fires, when user pressed a finished input button
        email.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (FormValidator.validateEmail((EditText) v)) {
                        //Add code to hide keyboard here
                        return true;
                    } else {
                        v.setError(getString(R.string.signUpEmailErrorMessage));
                        return false;
                    }
                }
                return false;
            }
        });
        //This listener fires, when the input lost focus
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!FormValidator.validateEmail((EditText) v)) {
                        ((EditText) v).setError(getString(R.string.signUpEmailErrorMessage));
                    }
                }
            }
        });

        //Attach validators for Username
        EditText user = (EditText) view.findViewById(R.id.signUpUserInput);
        user.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (FormValidator.validateUsername((EditText) v)) {
                        return true;
                    } else {
                        v.setError(getString(R.string.signUpUsernameErrorMessage));
                        return false;
                    }
                }
                return false;
            }
        });

        user.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!FormValidator.validateUsername((EditText) v)) {
                        ((EditText) v).setError(getString(R.string.signUpUsernameErrorMessage));
                    }
                }
            }
        });

        //Attach validators for password
        EditText password = (EditText) view.findViewById(R.id.signUpPasswordInput);
        password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (FormValidator.validatePassword((EditText) v)) {
                        return true;
                    } else {
                        v.setError(getString(R.string.signUpPasswordErrorMessage));
                        return false;
                    }
                }
                return false;
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!FormValidator.validatePassword((EditText) v)) {
                        ((EditText) v).setError(getString(R.string.signUpPasswordErrorMessage));
                    }
                }
            }
        });

        //Attach validators for phone number
        EditText phone = (EditText) view.findViewById(R.id.signUpPhoneInput);
        phone.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (FormValidator.validatePhone((EditText) v)) {
                        return true;
                    } else {
                        v.setError(getString(R.string.signUpPhoneErrorMessage));
                        return false;
                    }
                }
                return false;
            }
        });

        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!FormValidator.validatePassword((EditText) v)) {
                        ((EditText) v).setError(getString(R.string.signUpPhoneErrorMessage));
                    }
                }
            }
        });
        return view;
    }


    /**
     * *
     * Handle form submission
     *
     * @param view
     */
    public void onClick(View view) {
        boolean inputValid = true;

        //ultimately check user inputs
        if (!FormValidator.validateUsername(user)) {
            user.setError(getString(R.string.signUpUsernameErrorMessage));
            inputValid = false;
        }
        if (!FormValidator.validatePassword(pwd)) {
            pwd.setError(getString(R.string.signUpPasswordErrorMessage));
            inputValid = false;
        }
        if (!FormValidator.validateEmail(email)) {
            email.setError(getString(R.string.signUpEmailErrorMessage));
            inputValid = false;
        }
        if (!FormValidator.validatePhone(phone)) {
            phone.setError(getString(R.string.signUpPhoneErrorMessage));
            inputValid = false;
        }

        // if the user input is valid, sign him up and then go to the MyMoods screen
        if (inputValid) {

            Log.d(TAG, "Input valid. Signing up user...");
            // call to API handler to sign up user TODO

            // redirect to the MyMood by replacing the Signup fragment with MyMoodFragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new MyMoodFragment()).commit();
            Log.d(TAG, "Replaced fragment with MyMood");
        }



    }
}
