package com.github.panarik.mobile.app.shop.screen;

import com.github.panarik.mobile.app.shop.R;
import com.github.panarik.mobile.app.shop.base.TestBase;

import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class TestVideoWebBase extends TestBase {



    @Test
    public void editSearchMovie() {
        goToTestVideoWeb();
        onView(withId(R.id.editNameMovie))
                .perform(typeText("superman"))
                .check(matches(withText("superman")));
    }


    public void goToTestVideoWeb() {
        onView(withId(R.id.freeGame))
                .perform(click());
        onView(withId(R.id.anotherThings))
                .perform(click());
        onView(withId(R.id.toWatchVideo))
                .perform(click());
        onView(withId(R.id.toPlayingAudioActivity))
                .perform(click());
    }
}
