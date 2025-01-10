package com.inventory.tfgproject

import android.os.Handler
import android.os.Looper
import android.text.InputType
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.allOf
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.inventory.tfgproject.view.LoginScreen
import com.inventory.tfgproject.view.MainMenu
import com.inventory.tfgproject.viewmodel.AuthViewModel
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.hamcrest.Description
import org.junit.runner.RunWith
import org.hamcrest.Matcher
import org.mockito.ArgumentMatchers.matches
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginScreen::class.java)

    @Test
    fun verifyForgotPasswordVisibilityAndInteraction() {
        // Verify forgot password text is visible
        Espresso.onView(ViewMatchers.withId(R.id.txtForgotPassword))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.forgot_password)))

        // Click forgot password with empty email
        Espresso.onView(ViewMatchers.withId(R.id.txtForgotPassword))
            .perform(ViewActions.click())

        // Should show email error
        Espresso.onView(ViewMatchers.withId(R.id.emailLoginContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutHelperText("Introduce un correo válido")))
    }

    @Test
    fun verifyGoogleSignInButtonVisibilityAndInteraction() {
        // Verify Google sign-in elements are visible
        Espresso.onView(ViewMatchers.withId(R.id.imgGoogle))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.txtO))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.withText("o")))
    }

    @Test
    fun verifyProgressBarBehaviorDuringLogin() {
        Espresso.onView(ViewMatchers.withId(R.id.edtEmailLogin))
            .perform(ViewActions.typeText("leonardofb01@yopmail.com"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtPasswordLogin))
            .perform(ViewActions.typeText("Admin123"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.pbLogin))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        Espresso.onView(ViewMatchers.withId(R.id.btnLoginScreen))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.pbLogin))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.btnLoginScreen))
            .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun verifyPasswordToggleVisibility() {
        Espresso.onView(ViewMatchers.withId(R.id.edtPasswordLogin))
            .perform(ViewActions.typeText("Admin123"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.passwordLoginContainer))
            .check(ViewAssertions.matches(hasPasswordToggleDisplayed()))
    }

    @Test
    fun verifyEmailFieldBehavior() {
        ActivityScenario.launch(LoginScreen::class.java)

        // 2. Type an invalid email
        Espresso.onView(withId(R.id.edtEmailLogin))
            .perform(typeText("invalid_email"))
            .perform(ViewActions.closeSoftKeyboard())

        // 3. Remove focus to trigger validation
        Espresso.onView(withId(R.id.edtPasswordLogin))
            .perform(click())

        // 4. Check the helper text
        Espresso.onView(
            allOf(
                withId(R.id.emailLoginContainer),
                instanceOf(TextInputLayout::class.java)
            )
        ).check(ViewAssertions.matches(hasTextInputLayoutHelperText("Introduce un correo válido")))

        // 5. Type a valid email
        Espresso.onView(withId(R.id.edtEmailLogin))
            .perform(clearText())
            .perform(typeText("leonardofb01@yopmail.com"))
            .perform(ViewActions.closeSoftKeyboard())

        // 6. Remove focus again
        Espresso.onView(withId(R.id.edtPasswordLogin))
            .perform(click())

        // 7. Verify the helper text is gone
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
    private fun withTextInputLayoutHint(expectedHint: String?): Matcher<View> = object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("with helper text: $expectedHint")
        }

        override fun matchesSafely(item: TextInputLayout): Boolean {
            val helperText = item.helperText
            return if (expectedHint == null) {
                helperText.isNullOrEmpty()
            } else {
                expectedHint == helperText.toString()
            }
        }
    }
}