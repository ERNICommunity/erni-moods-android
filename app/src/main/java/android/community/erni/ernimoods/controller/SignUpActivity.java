package android.community.erni.ernimoods.controller;

import android.app.Activity;

import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.service.FormValidator;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Use this to sign up to the service...
 * Maybe it should be replaced by a preference, because once you do it you never really need to change that.
 *
 * Created by gus on 24.08.14.
 */
public class SignUpActivity  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Attach validators for eMail
        EditText email= (EditText) findViewById(R.id.signUpEmailInput);
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
        EditText user= (EditText) findViewById(R.id.signUpUserInput);
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
        EditText password= (EditText) findViewById(R.id.signUpPasswordInput);
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
        EditText phone= (EditText) findViewById(R.id.signUpPhoneInput);
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
    }


    /**
     * Handle form submission
     * @param view
     */
    public void createUser(View view){
        EditText user = (EditText) findViewById(R.id.signUpUserInput);
        EditText pwd = (EditText) findViewById(R.id.signUpPasswordInput);
        EditText email = (EditText) findViewById(R.id.signUpEmailInput);
        EditText phone = (EditText) findViewById(R.id.signUpPhoneInput);

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

            //forward to my moods
            Intent intent = new Intent(this, MyMoodActivity.class);
            startActivity(intent);
        }

    }

}
