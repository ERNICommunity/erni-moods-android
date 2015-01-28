package android.community.erni.ernimoods;

import android.app.Activity;
import android.community.erni.ernimoods.controller.EntryPoint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.robotium.solo.Condition;
import com.robotium.solo.Solo;

/**
 * Created by meru on 21.01.2015.
 */
public class UserActivityTestRobotium extends ActivityInstrumentationTestCase2<EntryPoint> {


    private Solo solo;
    private EntryPoint activity;
    public UserActivityTestRobotium() {
        super(EntryPoint.class);
    }

    private Condition falseCondition;

    @Override
    protected void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        falseCondition = new Condition() {
            @Override
            public boolean isSatisfied() {
                return false;
            }
        };
        logout();
    }

    public void test1UserLogin() {
        solo.clearEditText(0);
        solo.clearEditText(1);
        solo.enterText(0, "ruben");
        solo.enterText(1,"ruben");
        solo.clickOnButton("Login using an existing username");
        solo.waitForDialogToClose();
        assertTrue(solo.searchText("How are you today?"));
    }

    public void test2UserSignUp() {
        solo.clickOnText("Sign Up here");
        solo.enterText(0, "ruben");
        solo.enterText(1,"ruben");
        solo.enterText(2,"ruben.meier@erni.ch");
        solo.enterText(3,"0041763444053");
        solo.clickOnButton("Sign Up");
        solo.waitForDialogToClose();
        assertFalse(solo.searchButton("Sign Up",true));
    }

    //logout from moods
    private void logout(){
        if(!solo.searchButton("Login using an existing username", true)){
            solo.clickOnView(getActivity().findViewById(R.id.action_preferences));
            solo.clickOnText("Logout",2);
        }
    }
    @Override
    public void tearDown() throws Exception {
        logout();
        solo.finishOpenedActivities();
    }

}
