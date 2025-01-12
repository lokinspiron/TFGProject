package com.inventory.tfgproject

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inventory.tfgproject.view.LoginScreen
import com.inventory.tfgproject.view.MainMenu
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainMenuTest {

    private lateinit var scenario: ActivityScenario<MainMenu>
    private val testEmail = "leonardofb01@yopmail.com"
    private val testPassword = "Admin123"

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginScreen::class.java)

    @Before
    fun setup() {
        onView(withId(R.id.edtEmailLogin))
            .perform(typeText(testEmail), closeSoftKeyboard())
        onView(withId(R.id.edtPasswordLogin))
            .perform(typeText(testPassword), closeSoftKeyboard())
        onView(withId(R.id.btnLoginScreen))
            .perform(click())

        Thread.sleep(2000)

        scenario = ActivityScenario.launch(MainMenu::class.java)
    }

    @Test
    fun testNavigationDrawerOpensAndCloses() {
        onView(withId(R.id.toolbarDrawerToggle))
            .perform(click())
        onView(withId(R.id.nav_view))
            .check(matches(isDisplayed()))

        onView(withId(R.id.menudrawerlt))
            .perform(DrawerActions.close())
        onView(withId(R.id.nav_view))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun testBottomNavigationFunction() {

        onView(allOf(
            withId(R.id.btnProviders),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        ).perform(click())

        onView(withId(R.id.txtActivityWave))
            .check(matches(withText("Proveedores")))

        onView(withId(R.id.btnHome))
            .perform(click())
        onView(withId(R.id.txtActivityWave))
            .check(matches(withText(containsString("Hola"))))
    }

    @Test
    fun testFabMenuOpensInventory() {
        onView(allOf(
            withId(R.id.fabMainMenu),
            isDisplayed(),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
        )).perform(click())

        onView(withId(R.id.txtActivityWave))
            .check(matches(withText("Inventario")))
    }

    @Test
    fun testNavigationDrawerMenuItems() {
        onView(withId(R.id.toolbarDrawerToggle))
            .perform(click())

        onView(withId(R.id.nav_settings))
            .perform(click())
        onView(withId(R.id.txtActivityWave))
            .check(matches(withText("Perfil")))

        onView(withId(R.id.toolbarDrawerToggle))
            .perform(click())
        onView(withId(R.id.nav_about))
            .perform(click())
        onView(withId(R.id.txtActivityWave))
            .check(matches(withText("Acerca de nosotros")))
    }

    @Test
    fun testUserDataDisplayed() {
        onView(withId(R.id.toolbarDrawerToggle))
            .perform(click())

        onView(withId(R.id.txtEmailHeader))
            .check(matches(withText(testEmail)))

        onView(allOf(withId(R.id.txtActivityWave), withText("¡Hola, Leonardo!")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLogoutFlow() {
        onView(withId(R.id.toolbarDrawerToggle))
            .perform(click())
        onView(withId(R.id.nav_logout))
            .perform(click())

        onView(withText("Estás a punto de cerrar la sesión"))
            .check(matches(isDisplayed()))

        onView(withText("Cerrar Sesión"))
            .perform(click())

        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    @After
    fun cleanup() {
        scenario.close()
    }
}