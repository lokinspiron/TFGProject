package com.inventory.tfgproject

import android.text.Editable
import android.view.LayoutInflater
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.widget.Spinner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inventory.tfgproject.databinding.FragmentAddProductBinding
import com.inventory.tfgproject.viewmodel.ProductViewModel
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.view.AddProductFragment
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AddProductFragmentUnitTest {

    private lateinit var fragment: AddProductFragment
    private lateinit var binding: FragmentAddProductBinding

    @MockK
    private lateinit var productViewModel: ProductViewModel

    @MockK
    private lateinit var nameContainer: TextInputLayout

    @MockK
    private lateinit var quantityContainer: TextInputLayout

    @MockK
    private lateinit var priceContainer: TextInputLayout

    @MockK
    private lateinit var weightContainer: TextInputLayout

    @MockK
    private lateinit var nameEdit: TextInputEditText

    @MockK
    private lateinit var quantityEdit: TextInputEditText

    @MockK
    private lateinit var priceEdit: TextInputEditText

    @MockK
    private lateinit var weightEdit: TextInputEditText

    @MockK
    private lateinit var spinnerCategory: Spinner

    @MockK
    private lateinit var spinnerProvider: Spinner

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Crear el binding mock con relaxed = true
        binding = mockk(relaxed = true) {
            every { nameProductAddContainer } returns nameContainer
            every { quantityProductAddContainer } returns quantityContainer
            every { priceProductAddContainer } returns priceContainer
            every { weightProductAddContainer } returns weightContainer
            every { edtNameProductAdd } returns nameEdit
            every { edtQuantityProductAdd } returns quantityEdit
            every { edtPriceProductAdd } returns priceEdit
            every { edtWeightProductAdd } returns weightEdit
            every { spinnerCategory } returns spinnerCategory
            every { spinnerProvider } returns spinnerProvider
        }

        // Inicializar el fragmento
        fragment = AddProductFragment()
        fragment._binding = binding

        // Inyectar el ViewModel
        val field = AddProductFragment::class.java.getDeclaredField("productViewModel")
        field.isAccessible = true
        field.set(fragment, productViewModel)
    }

    @Test
    fun `saveProduct creates product with correct values when inputs are valid`() {
        // Setup
        every { nameEdit.text?.toString() } returns "Test Product"
        every { quantityEdit.text?.toString() } returns "10"
        every { priceEdit.text?.toString() } returns "100.00"
        every { weightEdit.text?.toString() } returns "1.5"

        val mockCategory = Category(id = "1", name = "Test Category")
        val mockProvider = Providers(id = "1", name = "Test Provider")

        every { spinnerCategory.selectedItem } returns mockCategory
        every { spinnerProvider.selectedItem } returns mockProvider
        every { productViewModel.saveProduct(any()) } just Runs

        // Execute
        fragment.saveProduct()

        // Verify
        verify { productViewModel.saveProduct(match { product ->
            product.name == "Test Product" &&
                    product.stock == 10 &&
                    product.price == 100.00 &&
                    product.weight == 1.5 &&
                    product.categoryId == "1" &&
                    product.providerId == "1"
        })}
    }

    @Test
    fun `clearForm resets all fields`() {
        // Setup
        every { nameEdit.text?.clear() } just Runs
        every { quantityEdit.text?.clear() } just Runs
        every { priceEdit.text?.clear() } just Runs
        every { weightEdit.text?.clear() } just Runs
        every { spinnerCategory.setSelection(0) } just Runs
        every { spinnerProvider.setSelection(0) } just Runs

        // Execute
        fragment.clearForm()

        // Verify
        verify {
            nameEdit.text?.clear()
            quantityEdit.text?.clear()
            priceEdit.text?.clear()
            weightEdit.text?.clear()
            spinnerCategory.setSelection(0)
            spinnerProvider.setSelection(0)
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }
}