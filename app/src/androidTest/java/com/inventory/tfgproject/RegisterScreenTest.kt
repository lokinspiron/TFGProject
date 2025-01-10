package com.inventory.tfgproject

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
import com.inventory.tfgproject.view.RegisterScreen
import com.inventory.tfgproject.view.RegisterScreenInfo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    private lateinit var scenario: ActivityScenario<RegisterScreen>

    @Before
    fun setUp() {
        Intents.init()
        scenario = ActivityScenario.launch(RegisterScreen::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
        Intents.release()
    }

    @Test
    fun checkInitialVisibility() {
        Espresso.onView(ViewMatchers.withId(R.id.txtRegister))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.btnContinueRegister))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))

        Espresso.onView(ViewMatchers.withId(R.id.imgGoogle))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun emptyFieldsShowError() {
        Espresso.onView(ViewMatchers.withId(R.id.btnContinueRegister))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.nameRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Nombre(s) requerido(s)")))

        Espresso.onView(ViewMatchers.withId(R.id.surnameRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Apellido(s) requerido(s)")))

        Espresso.onView(ViewMatchers.withId(R.id.emailRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Correo inválido")))

        Espresso.onView(ViewMatchers.withId(R.id.passwordRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Contraseña requerida")))

        Espresso.onView(ViewMatchers.withId(R.id.confirmPasswordRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Confirmar contraseña requerida")))
    }

    @Test
    fun invalidEmailShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.edtEmailRegister))
            .perform(ViewActions.typeText("invalidemail"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtPasswordsRegister))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.emailRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Introduce un correo válido")))
    }

    @Test
    fun invalidNameShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.edtNameRegister))
            .perform(ViewActions.typeText("nombre"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtSurnameRegister))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.nameRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Debe contener por lo menos una mayúscula")))
    }

    @Test
    fun invalidSurnameShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.edtSurnameRegister))
            .perform(ViewActions.typeText("apellido"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtEmailRegister))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.surnameRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Debe contener por lo menos una mayúscula")))
    }

    @Test
    fun passwordsMismatchShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.edtPasswordsRegister))
            .perform(ViewActions.typeText("Password123"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtConfirmPasswordRegister))
            .perform(ViewActions.typeText("Password124"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtEmailRegister))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.confirmPasswordRegisterContainer))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText("Las contraseñas no coinciden")))
    }

    @Test
    fun validRegistrationFlow() {
        Espresso.onView(ViewMatchers.withId(R.id.edtNameRegister))
            .perform(ViewActions.typeText("Leonardo"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtSurnameRegister))
            .perform(ViewActions.typeText("Farfan"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtEmailRegister))
            .perform(ViewActions.typeText("test0101@yopmail.com"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtPasswordsRegister))
            .perform(ViewActions.typeText("Admin123"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withId(R.id.edtConfirmPasswordRegister))
            .perform(
                ViewActions.typeText("Admin123"),
                ViewActions.closeSoftKeyboard(),
                ViewActions.pressImeActionButton()
            )

        Thread.sleep(1000)

        Espresso.onView(ViewMatchers.withId(R.id.btnContinueRegister))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))

        val expectedIntent = hasComponent(RegisterScreenInfo::class.java.name)

        Intents.intending(IntentMatchers.anyIntent()).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )

        Espresso.onView(ViewMatchers.withId(R.id.btnContinueRegister))
            .perform(ViewActions.click())

        intended(expectedIntent)
    }

    private fun hasTextInputLayoutErrorText(expectedErrorText: String): org.hamcrest.Matcher<android.view.View> {
        return object : BoundedMatcher<android.view.View,TextInputLayout>(
            com.google.android.material.textfield.TextInputLayout::class.java
        ) {
            override fun matchesSafely(item: TextInputLayout): Boolean {
                val error = item.helperText
                return expectedErrorText == error?.toString()
            }

            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("has error text: $expectedErrorText")
            }
        }
    }
}