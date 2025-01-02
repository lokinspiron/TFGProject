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
import com.inventory.tfgproject.ProductLowAdapter
import com.inventory.tfgproject.ProductRepository
import com.inventory.tfgproject.ProductViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.EmptyStateBinding
import com.inventory.tfgproject.databinding.FragmentMenuMainBinding
import com.inventory.tfgproject.viewmodel.OrderViewModel
import com.inventory.tfgproject.viewmodel.ProductViewModel


class MenuMainFragment : Fragment() {
    private lateinit var binding : FragmentMenuMainBinding
    private lateinit var emptyStateBinding: EmptyStateBinding
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var productLowAdapter : ProductLowAdapter

    private val orderViewModel: OrderViewModel by viewModels{
        OrderViewModelFactory(OrderRepository())
    }
    private val productViewModel: ProductViewModel by viewModels{
        ProductViewModelFactory(ProductRepository())
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
        observeProducts()
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
            val pendingOrders = orders.filter { it.order.estado?.lowercase() == "pendiente" }
            orderAdapter.setOrders(pendingOrders)

        }
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("MenuMainFragment", "Received products: ${products.size}")
            productLowAdapter.updateProducts(products)
        }
    }

    private fun updateVisibilities(isLoading: Boolean, hasPendingOrders: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleVisibilityStates(isLoading: Boolean, isEmpty: Boolean) {
        binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            val isLoading = orderViewModel.isLoading.value ?: false

            if (isLoading) {
                loadingProgressBar.visibility = View.VISIBLE
                root.findViewById<ConstraintLayout>(R.id.emptyStateContainer).visibility = View.GONE
                rvOrders.visibility = View.GONE
                return@apply
            }

            loadingProgressBar.visibility = View.GONE
            root.findViewById<ConstraintLayout>(R.id.emptyStateContainer).visibility =
                if (isEmpty) View.VISIBLE else View.GONE
            rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            orders = mutableListOf(),
            maxItems = maxOrders,
            emptyStateBinding = emptyStateBinding,
            recyclerView = binding.rvOrders
        )
        binding.rvOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        val emptyStateLowStock = binding.root.findViewById<View>(R.id.emptyStateLowStock)
        productLowAdapter = ProductLowAdapter(
            emptyStateContainer = emptyStateLowStock,
            recyclerView = binding.rvLowStockProducts,
            loadingProgressBar = binding.root.findViewById(R.id.loadingProgressBarLowStock)
        )

        binding.rvLowStockProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productLowAdapter
        }
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
                    (activity as? MainMenu)?.replaceFragment(InventoryFragment())
                    true
                }
                R.id.btnProviders -> {
                    (activity as? MainMenu)?.replaceFragment(ProviderFragment())
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
        productViewModel.loadProducts()
    }

    override fun onResume() {
        super.onResume()
        refreshOrders()
    }

}