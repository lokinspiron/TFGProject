package com.inventory.tfgproject.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inventory.tfgproject.OrderAdapter
import com.inventory.tfgproject.OrderRepository
import com.inventory.tfgproject.OrderViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.EmptyStateBinding
import com.inventory.tfgproject.databinding.FragmentMenuMainBinding
import com.inventory.tfgproject.viewmodel.OrderViewModel


class MenuMainFragment : Fragment() {
    private lateinit var binding : FragmentMenuMainBinding
    private lateinit var emptyStateBinding: EmptyStateBinding
    private lateinit var orderAdapter: OrderAdapter
    private val orderViewModel: OrderViewModel by viewModels{
        OrderViewModelFactory(OrderRepository())
    }

    private var maxOrders = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuMainBinding.inflate(inflater,container,false)
        emptyStateBinding = EmptyStateBinding.bind(binding.root.findViewById(R.id.emptyStateContainer))
        initListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        refreshOrders()
        observeOrders()
        observeLoadingState()
    }

    private fun observeLoadingState() {
        orderViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            handleVisibilityStates(isLoading, orderViewModel.ordersWithProducts.value?.isEmpty() ?: true)
        }
    }

    private fun observeOrders() {
        orderViewModel.ordersWithProducts.observe(viewLifecycleOwner) { orders ->
            Log.d("MenuMainFragment", "Received orders: ${orders.size}")
            orderAdapter.setOrders(orders)
            handleVisibilityStates(orderViewModel.isLoading.value ?: false, orders.isEmpty())
        }
    }

    private fun handleVisibilityStates(isLoading: Boolean, isEmpty: Boolean) {
        binding.apply {
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            root.findViewById<ConstraintLayout>(R.id.emptyStateContainer).visibility =
                if (!isLoading && isEmpty) View.VISIBLE else View.GONE
            rvOrders.visibility =
                if (!isLoading && !isEmpty) View.VISIBLE else View.GONE
            btnMoreOrders.visibility =
                if (!isLoading && !isEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            val isLoading = orderViewModel.isLoading.value ?: false

            if (isLoading) {
                loadingProgressBar.visibility = View.VISIBLE
                root.findViewById<ConstraintLayout>(R.id.emptyStateContainer).visibility = View.GONE
                rvOrders.visibility = View.GONE
                btnMoreOrders.visibility = View.GONE
                return@apply
            }

            loadingProgressBar.visibility = View.GONE
            root.findViewById<ConstraintLayout>(R.id.emptyStateContainer).visibility =
                if (isEmpty) View.VISIBLE else View.GONE
            rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
            btnMoreOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            orders = mutableListOf(),
            maxItems = maxOrders,
            onQuantityChanged = { order, newQuantity ->
                orderViewModel.updateOrderQuantity(order, newQuantity)
            }
        )
        binding.rvOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(context)
        }
        handleVisibilityStates(true, true)
    }

    private fun initListeners() {
        val btnDrawerToggle= binding.root.findViewById<ImageButton>(R.id.btnDrawerToggle)
        val drawerlt = binding.root.findViewById<DrawerLayout>(R.id.drawerlt)
        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }

        val bnvMenu = binding.root.findViewById<BottomNavigationView>(R.id.bnvMenu)
        bnvMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    replaceFragment(InventoryFragment())
                    true
                }
                R.id.btnProviders -> {
                    replaceFragment(ProviderFragment())
                    true
                }
                else -> false
            }
        }

        binding.btnMoreOrders.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(OrdersFragment(), "Pedidos")
        }

    }

    private fun replaceFragment(fragment: Fragment, greetingMessage: String?=null)  {
        val fragmentTag = fragment.javaClass.simpleName

        val fragmentTransaction = childFragmentManager.beginTransaction()

        val existingFragment = childFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.fcvContent, fragment, fragmentTag)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        } else {
            fragmentTransaction.show(existingFragment)
            fragmentTransaction.commit()
        }

        greetingMessage?.let {
            binding.txtWave.text = it
        }
    }

    private fun refreshOrders() {
        Log.d("MenuMainFragment", "Refreshing orders")
        orderViewModel.loadOrders()
    }

    override fun onResume() {
        super.onResume()
        refreshOrders()
    }

}