package com.inventory.tfgproject

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.view.AddProductFragment
import com.inventory.tfgproject.viewmodel.ProductViewModel
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import kotlin.math.absoluteValue

@RunWith(AndroidJUnit4::class)
class AddProductTest {
    private lateinit var productViewModel: ProductViewModel
    private lateinit var scenario: FragmentScenario<AddProductFragment>

    @Before
    fun setup() {
        try {
            // Use mockito-kotlin's mock function
            productViewModel = mock()

            // Use 'whenever' instead of when
            `when`(productViewModel.categories).thenReturn(
                MutableLiveData(listOf(
                    Category(id = "1", name = "Test Category")
                ))
            )
            `when`(productViewModel.providers).thenReturn(
                MutableLiveData(listOf(
                    Providers(id = "1", name = "Test Provider")
                ))
            )

            scenario = launchFragmentInContainer(
                fragmentArgs = Bundle(),
                themeResId = R.style.Theme_TFGProject
            ) {
                AddProductFragment().apply {
                    customViewModelFactory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return productViewModel as T
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw AssertionError("Failed to setup test: ${e.message}", e)
        }
    }

    @Test
    fun whenAddValidProduct_thenProductIsSaved() {
        onView(withId(R.id.edtNameProductAdd))
            .perform(typeText("Test Product"), closeSoftKeyboard())
        onView(withId(R.id.edtQuantityProductAdd))
            .perform(typeText("10"), closeSoftKeyboard())
        onView(withId(R.id.edtPriceProductAdd))
            .perform(typeText("99.99"), closeSoftKeyboard())
        onView(withId(R.id.edtWeightProductAdd))
            .perform(typeText("1.5"), closeSoftKeyboard())

        onView(withId(R.id.spinnerCategory)).perform(click())
        onView(withText("Test Category")).perform(click())
        onView(withId(R.id.spinnerProvider)).perform(click())
        onView(withText("Test Provider")).perform(click())

        onView(withId(R.id.btnSaveChangesAddProduct)).perform(click())

        val argumentCaptor = ArgumentCaptor.forClass(Product::class.java)
        verify(productViewModel).saveProduct(argumentCaptor.capture())

        val savedProduct = argumentCaptor.value
        with(savedProduct) {
            assert(name == "Test Product") { "Product name mismatch: expected 'Test Product', got '$name'" }
            assert(stock == 10) { "Stock mismatch: expected 10, got $stock" }
            assert((price - 99.99).absoluteValue < 0.01) { "Price mismatch: expected 99.99, got $price" }
            assert((weight - 1.5).absoluteValue < 0.01) { "Weight mismatch: expected 1.5, got $weight" }
            assert(categoryId == "1") { "Category ID mismatch: expected '1', got $categoryId" }
            assert(providerId == "1") { "Provider ID mismatch: expected '1', got $providerId" }
        }
    }

    @Test
    fun whenMissingRequiredFields_thenShowsErrors() {
        onView(withId(R.id.btnSaveChangesAddProduct)).perform(click())

        onView(withText("Nombre es requerido"))
            .check(matches(isDisplayed()))
        onView(withText("Cantidad es requerida"))
            .check(matches(isDisplayed()))
        onView(withText("Precio es requerido"))
            .check(matches(isDisplayed()))
        onView(withText("Peso es requerido"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun whenCancelAddProduct_thenNavigatesBack() {
        onView(withId(R.id.btnCancelAddProduct)).perform(click())

        scenario.onFragment { fragment ->
            assertTrue("Fragment should be marked for removal", fragment.isRemoving)
        }
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }
}