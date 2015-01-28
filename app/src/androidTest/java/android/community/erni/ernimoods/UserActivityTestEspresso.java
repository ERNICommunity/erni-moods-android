package android.community.erni.ernimoods;

import android.community.erni.ernimoods.controller.EntryPoint;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.test.ActivityInstrumentationTestCase2;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import android.test.suitebuilder.annotation.LargeTest;

/**
 * Created by meru on 27.01.2015.
 */
@LargeTest
public class UserActivityTestEspresso extends ActivityInstrumentationTestCase2<EntryPoint> {

    public UserActivityTestEspresso() {
        super(EntryPoint.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testLogin() {
//        Espresso.openContextualActionModeOverflowMenu();
        onView(withId(R.id.email_login_form)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.username_loginform)).perform(ViewActions.typeText("ruben"));
        onView(withId(R.id.password_loginform)).perform(ViewActions.typeText("ruben"));
        onView(withId(R.id.login_loginform)).perform(ViewActions.click());
        onView(withId(R.id.textViewGreeting)).check(ViewAssertions.matches(isDisplayed()));

        //logout();
        //logout
//        onView(withId(R.id.action_preferences)).check(ViewAssertions.matches(isDisplayed()));
//        onView(withId(R.id.action_preferences)).perform(ViewActions.click());
//        onView(withText("Logout")).perform((ViewActions.click()));
    }

    @Override
    public void tearDown() throws Exception {
        logout();
    }
    private void logout(){
//        onView(withId(R.id.action_preferences)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.action_preferences)).perform(ViewActions.click());
        onView(withText("Logout User")).perform((ViewActions.click()));
    }
}
