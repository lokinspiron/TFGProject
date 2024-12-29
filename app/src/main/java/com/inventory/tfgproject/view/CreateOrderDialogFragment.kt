package com.inventory.tfgproject.view

import android.R
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.inventory.tfgproject.OrderRepository
import com.inventory.tfgproject.OrderViewModelFactory
import com.inventory.tfgproject.ProductRepository
import com.inventory.tfgproject.ProductViewModelFactory
import com.inventory.tfgproject.ProviderRepository
import com.inventory.tfgproject.ProviderViewModelFactory
import com.inventory.tfgproject.databinding.FragmentDialogCreateOrderBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Orders
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.viewmodel.OrderViewModel
import com.inventory.tfgproject.viewmodel.ProductViewModel
import com.inventory.tfgproject.viewmodel.ProviderViewModel
import java.util.Calendar

class CreateOrderDialogFragment: DialogFragment() {
    private lateinit var binding : FragmentDialogCreateOrderBinding
    private var providerId: String? = null
    private var productId: String? = null

    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository())
    }

    companion object {
        fun newInstance(providerId: String,productId: String): CreateOrderDialogFragment {
            val fragment = CreateOrderDialogFragment()
            val args = Bundle()
            args.putString("provider_id", providerId)
            args.putString("product_id", productId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        providerId = arguments?.getString("provider_id")
        productId = arguments?.getString("product_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogCreateOrderBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEditText()
        initListeners()
        setupDateFormatter()
        setupSpinner()
    }

    private fun initListeners() {
        binding.btnMakeOrder.setOnClickListener {
            saveOrder()
        }
        binding.imgClose.setOnClickListener{
            dismiss()
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (binding.edtQuantityOrder.text.isNullOrBlank()) {
            binding.quantityOrderContainer.error = "Cantidad es requerida"
            isValid = false
        }

        if (binding.edtDateOrder.text.isNullOrBlank()) {
            binding.dateOrderContainer.error = "Fecha es requerida"
            isValid = false
        } else {
            val dateStr = binding.edtDateOrder.text.toString()
            if (!isValidDate(dateStr, Calendar.getInstance().get(Calendar.YEAR))) {
                binding.dateOrderContainer.error = "Fecha inválida o pasada"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveOrder(){
        if(validateInput()){
            val quantity = binding.edtQuantityOrder.text.toString().toIntOrNull() ?: 0
            val partialDate = binding.edtDateOrder.text.toString()
            val fullDate = getFullDate(partialDate)
            val provider = binding.spinnerProvider.selectedItem as? Providers

            if(provider == null || provider.name == "Selecciona un proveedor"){
                toast("Selecciona proveedor", LENGTH_SHORT)
                return
            }

           val order = Orders(
               fechaPedido = fullDate,
               stock = quantity,
               estado = "Pendiente",
               proveedorId = provider.id,
               productId = productId ?: ""
            )

            orderViewModel.saveOrder(order)
            clearForm()
            setupSpinner()
        }
    }

    private fun clearForm() {
        binding.spinnerProvider.setSelection(0)
        binding.edtQuantityOrder.text?.clear()
        binding.edtDateOrder.text?.clear()
    }

    private fun setupSpinner(){
        orderViewModel.loadProviders()
        orderViewModel.providers.observe(viewLifecycleOwner) { providers ->
            updateProviderSpinner(providers)
        }
    }

    private fun initEditText(){
        binding.dateOrderContainer.helperText = null
        binding.quantityOrderContainer.helperText = null
    }

    private fun setupDateFormatter() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        binding.edtDateOrder.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]|\\.".toRegex(), "")
                    if (clean.length > 4) {
                        clean = clean.substring(0, 4)
                    }

                    when (clean.length) {
                        2 -> clean += "/"
                        4 -> {
                            clean = clean.substring(0, 2) + "/" + clean.substring(2, 4)
                            // Validar la fecha ingresada
                            if (isValidDate(clean, currentYear)) {
                                binding.dateOrderContainer.error = null
                            } else {
                                binding.dateOrderContainer.error = "Fecha inválida"
                            }
                        }
                    }

                    current = clean
                    binding.edtDateOrder.setText(clean)
                    binding.edtDateOrder.setSelection(clean.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun updateProviderSpinner(providers: List<Providers>) {
        val itemList = mutableListOf(Providers(name = "Selecciona un proveedor"))
        itemList.addAll(providers)

        val adapter = object : ArrayAdapter<Providers>(
            requireContext(),
            R.layout.simple_spinner_item,
            itemList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = adapter

        val providerPosition = itemList.indexOfFirst { it.id == providerId }
        if (providerPosition != -1) {
            binding.spinnerProvider.setSelection(providerPosition)
        }
    }

    private fun isValidDate(dateStr: String, currentYear: Int): Boolean {
        try {
            val parts = dateStr.split("/")
            if (parts.size != 2) return false

            val day = parts[0].toInt()
            val month = parts[1].toInt()

            if (month < 1 || month > 12) return false
            if (day < 1 || day > 31) return false

            val maxDays = when (month) {
                2 -> if (currentYear % 4 == 0) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            if (day > maxDays) return false

            val calendar = Calendar.getInstance()
            val currentDate = Calendar.getInstance()

            calendar.set(currentYear, month - 1, day)


            return calendar.timeInMillis > currentDate.timeInMillis
        } catch (e: Exception) {
            return false
        }
    }

    private fun getFullDate(dateStr: String): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return "$dateStr/$currentYear"
    }

}