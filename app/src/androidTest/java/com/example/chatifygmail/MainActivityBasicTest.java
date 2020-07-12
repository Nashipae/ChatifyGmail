package com.example.chatifygmail;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityBasicTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void clickSenderItem_OpensShowMailsActivity() {
        // Find the view & Perform action on view
        onView((withId(R.id.recyclerViewTasks)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check if the view does as expected
        onView(withId(R.id.show_mails_linear_layout)).check(matches(isDisplayed()));
    }

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule2 =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void clickFabButton_OpensAddSenderActivity() {
        // Find the view & Perform action on view
        onView((withId(R.id.fab)))
                .perform(click());

        // Check if the view does as expected
        onView(withId(R.id.linearLayout)).check(matches(isDisplayed()));
    }
}
