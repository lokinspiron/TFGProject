package com.inventory.tfgproject

import org.hamcrest.Matchers.allOf
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.inventory.tfgproject.view.LoginScreen
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.hamcrest.Description
import org.junit.runner.RunWith
import org.hamcrest.Matcher

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginScreen::class.java)

    @Test
    fun verifyForgotPasswordVisibilityAndInteraction() {
        Espresso.onView(withId(R.id.txtForgotPassword))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.forgot_password)))

        Espresso.onView(withId(R.id.txtForgotPassword))
            .perform(click())

        Espresso.onView(withId(R.id.emailLoginContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutHelperText("Introduce un correo válido")))
    }

    @Test
    fun verifyGoogleSignInButtonVisibilityAndInteraction() {
        Espresso.onView(withId(R.id.imgGoogle))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun verifyProgressBarBehaviorDuringLogin() {
        Espresso.onView(withId(R.id.edtEmailLogin))
            .perform(typeText("leonardofb01@yopmail.com"), ViewActions.closeSoftKeyboard())

        Espresso.onView(withId(R.id.edtPasswordLogin))
            .perform(typeText("Admin123"), ViewActions.closeSoftKeyboard())

        Espresso.onView(withId(R.id.pbLogin))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        Espresso.onView(withId(R.id.btnLoginScreen))
            .perform(click())

        Espresso.onView(withId(R.id.pbLogin))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.btnLoginScreen))
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun verifyPasswordToggleVisibility() {
        Espresso.onView(withId(R.id.edtPasswordLogin))
            .perform(typeText("Admin123"), ViewActions.closeSoftKeyboard())

        Espresso.onView(withId(R.id.passwordLoginContainer))
            .check(ViewAssertions.matches(hasPasswordToggleDisplayed()))
    }

    @Test
    fun verifyEmailFieldBehavior() {
        ActivityScenario.launch(LoginScreen::class.java)

        Espresso.onView(withId(R.id.edtEmailLogin))
            .perform(typeText("invalid_email"))
            .perform(ViewActions.closeSoftKeyboard())

        Espresso.onView(withId(R.id.edtPasswordLogin))
            .perform(click())

        Espresso.onView(
            allOf(
                withId(R.id.emailLoginContainer),
                instanceOf(TextInputLayout::class.java)
            )
        ).check(ViewAssertions.matches(hasTextInputLayoutHelperText("Introduce un correo válido")))

        Espresso.onView(withId(R.id.edtEmailLogin))
            .perform(clearText())
            .perform(typeText("leonardofb01@yopmail.com"))
            .perform(ViewActions.closeSoftKeyboard())

        Espresso.onView(withId(R.id.edtPasswordLogin))
            .perform(click())

        Espresso.onView(
            allOf(
                withId(R.id.emailLoginContainer),
                instanceOf(TextInputLayout::class.java)
            )
        ).check(ViewAssertions.matches(hasTextInputLayoutHelperText(null)))
    }


    private fun hasPasswordToggleDisplayed(): Matcher<View> {
        return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            override fun describeTo(description: Description?) {
                description?.appendText("has password toggle displayed")
            }

            override fun matchesSafely(item: TextInputLayout): Boolean {
                return item.isPasswordVisibilityToggleEnabled
            }
        }
    }

    private fun hasTextInputLayoutHelperText(expectedErrorText: String?): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText("has helper text: $expectedErrorText")
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false
            val helperText = item.helperText
            if (expectedErrorText == null) {
                return helperText == null
            }
            return helperText == expectedErrorText
        }
    }

}