package com.example.chatifygmail;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.chatifygmail.database.Sender;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class ShowMailsActivityBasicTest {
    private static final Intent intent;
    private static ShowMailsActivity mActivity;

    static {
        intent = new Intent(ApplicationProvider.getApplicationContext(), ShowMailsActivity.class);
        Sender sender = new Sender("testmail@gmail.com", 0, null);
        intent.putExtra("Email Details", sender);

    }
    /*@Before
    public void setUp() {
        try (ActivityScenario<ShowMailsActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> mActivity = activity);
        }
    }*/

    @Rule
    public ActivityScenarioRule<ShowMailsActivity> activityScenarioRule = new ActivityScenarioRule<>(intent);

    @Test
    public void checkCorrectDetails_onLaunchWhenNoMails() {
        //onView(withText("testmail@gmail.com")).check(matches(isDisplayed()));
        //onView(withText("You have no unread emails from this sender")).check(matches(isDisplayed()));
        onView(withId(R.id.sender_header)).check(matches(withText("testmail@gmail.com")));
        //onView(withText("testmail@gmail.com")).check(matches(isDisplayed()));
        onView(withText("You have no unread emails from this sender")).check(matches(isDisplayed()));
    }
}
