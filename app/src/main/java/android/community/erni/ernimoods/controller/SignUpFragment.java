package android.community.erni.ernimoods.controller;

import android.app.Activity;
import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.service.FormValidator;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/** Fragment to handle user registration
 * Created by gus on 24.08.14.
 */
public class SignUpFragment extends Fragment {

    EditText user;
    EditText pwd;
    EditText email;
    EditText phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        user = (EditText) view.findViewById(R.id.signUpUserInput);
        pwd = (EditText) view.findViewById(R.id.signUpPasswordInput);
        email = (EditText) view.findViewById(R.id.signUpEmailInput);
        phone = (EditText) view.findViewById(R.id.signUpPhoneInput);

        //Attach validators for eMail
        EditText email= (EditText) view.findViewById(R.id.signUpEmailInput);
        //This listener fires, when user pressed a finished input button
        email.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(FormValidator.validateEmail((EditText)v)){
                        //Add code to hide keyboard here
                        return true;
                    }else{
                        v.setError(getString(R.string.signUpEmailErrorMessage));
                        return false;
                    }
                }
                return false;
            }
        });
        //This listener fires, when the input lost focus
        email.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    if(!FormValidator.validateEmail((EditText) v)){
                        ((EditText) v).setError(getString(R.string.signUpEmailErrorMessage));
                    }
                }
            }
        });

        //Attach validators for Username
        EditText user= (EditText) view.findViewById(R.id.signUpUserInput);
        user.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(FormValidator.validateUsername((EditText) v)){
                        return true;
                    }else{
                        v.setError(getString(R.string.signUpUsernameErrorMessage));
                        return false;
                    }
                }
                return false;
            }
        });

        user.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    if(!FormValidator.validateUsername((EditText) v)){
                        ((EditText) v).setError(getString(R.string.signUpUsernameErrorMessage));
                    }
                }
            }
        });

        //Attach validators for password
        EditText password= (EditText) view.findViewById(R.id.signUpPasswordInput);
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
        EditText phone= (EditText) view.findViewById(R.id.signUpPhoneInput);
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


    /** TODO this is going to need refactoring
     * throws  java.lang.IllegalStateException: Could not find a method createUser(View)
     *
     * Handle form submission
     * @param view
     */
    public void createUser(View view){

        boolean inputValid = true;

        //ultimately check user inputs
        if(!FormValidator.validateUsername(user)){
            user.setError(getString(R.string.signUpUsernameErrorMessage));
            inputValid = false;
        }
        if(!FormValidator.validatePassword(pwd)){
            pwd.setError(getString(R.string.signUpPasswordErrorMessage));
            inputValid = false;
        }
        if(!FormValidator.validateEmail(email)){
            email.setError(getString(R.string.signUpEmailErrorMessage));
            inputValid = false;
        }
        if(!FormValidator.validatePhone(phone)){
            phone.setError(getString(R.string.signUpPhoneErrorMessage));
            inputValid = false;
        }

        //make sure, that no error messages are set
        if(inputValid){
            /*
            Add code to create user here
             */

            // TODO replace wit mymoods fragment

        }

    }

}
