package com.inventory.tfgproject.view

import android.R
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.inventory.tfgproject.repository.OrderRepository
import com.inventory.tfgproject.modelFactory.OrderViewModelFactory
import com.inventory.tfgproject.repository.ProviderRepository
import com.inventory.tfgproject.modelFactory.ProviderViewModelFactory
import com.inventory.tfgproject.databinding.FragmentDialogCreateOrderBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Orders
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.viewmodel.OrderViewModel
import com.inventory.tfgproject.viewmodel.ProviderViewModel
import java.util.Calendar

class CreateOrderDialogFragment: DialogFragment() {
    private lateinit var binding : FragmentDialogCreateOrderBinding
    private var providerId: String? = null
    private var productId: String? = null

    private val providerViewModel: ProviderViewModel by viewModels {
        ProviderViewModelFactory(ProviderRepository())
    }

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
        setupGmailButton()
        setupPhoneButton()
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

    private fun setupSpinner() {
        orderViewModel.loadProviders()
        orderViewModel.providers.observe(viewLifecycleOwner) { providers ->
            updateProviderSpinner(providers)
        }

        binding.spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedProvider = parent?.getItemAtPosition(position) as? Providers
                orderViewModel.setSelectedProvider(selectedProvider)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                orderViewModel.setSelectedProvider(null)
            }
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
        val defaultProvider = Providers(name = "Selecciona un proveedor")
        val itemList = mutableListOf(defaultProvider)
        val customTypeface = ResourcesCompat.getFont(requireContext(), com.inventory.tfgproject.R.font.convergence)

        providers.forEach { provider ->
            Log.d("CreateOrderDialog", "Adding provider: $provider")
            itemList.add(provider.copy())
        }

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
                textView.typeface = customTypeface
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.typeface = customTypeface
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

    private fun setupGmailButton() {
        binding.btnGmail.setOnClickListener {
            val provider = binding.spinnerProvider.selectedItem as? Providers
            if(provider?.email.isNullOrEmpty()) {
                toast("No hay correo electrónico registrado para este proveedor", LENGTH_SHORT)
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${provider?.email}")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                toast("No se encontró aplicación de correo", LENGTH_SHORT)
            }
        }
    }

    private fun setupPhoneButton() {
        binding.btnPhone.setOnClickListener {
            val provider = binding.spinnerProvider.selectedItem as? Providers
            Log.d("CreateOrderDialog", "Provider: ${provider?.toString()}")
            Log.d("CreateOrderDialog", "Phone: ${provider?.phoneNumber}")

            if (provider?.phoneNumber.isNullOrEmpty()) {
                toast("No hay número de teléfono registrado para este proveedor", LENGTH_SHORT)
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${provider?.phoneNumber}")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                toast("No se pudo iniciar la aplicación de teléfono", LENGTH_SHORT)
            }
        }
    }

}