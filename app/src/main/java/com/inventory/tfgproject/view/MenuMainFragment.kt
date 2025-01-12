package com.inventory.tfgproject.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.inventory.tfgproject.adapter.OrderAdapter
import com.inventory.tfgproject.repository.OrderRepository
import com.inventory.tfgproject.modelFactory.OrderViewModelFactory
import com.inventory.tfgproject.adapter.ProductLowAdapter
import com.inventory.tfgproject.repository.ProductRepository
import com.inventory.tfgproject.modelFactory.ProductViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.adapter.BaseListAdapter
import com.inventory.tfgproject.adapter.ProvidersMenuAdapter
import com.inventory.tfgproject.databinding.EmptyStateBinding
import com.inventory.tfgproject.databinding.FragmentMenuMainBinding
import com.inventory.tfgproject.model.OrderWithProduct
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.modelFactory.ProviderViewModelFactory
import com.inventory.tfgproject.repository.ProviderRepository
import com.inventory.tfgproject.viewmodel.OrderViewModel
import com.inventory.tfgproject.viewmodel.ProductViewModel
import com.inventory.tfgproject.viewmodel.ProviderViewModel


class MenuMainFragment : Fragment() {
    private lateinit var binding : FragmentMenuMainBinding
    private lateinit var orderAdapter: BaseListAdapter<OrderWithProduct>
    private lateinit var productLowAdapter: BaseListAdapter<Product>
    private lateinit var providerMenuAdapter: BaseListAdapter<Providers>

    private val orderViewModel: OrderViewModel by viewModels{
        OrderViewModelFactory(OrderRepository())
    }
    private val productViewModel: ProductViewModel by viewModels{
        ProductViewModelFactory(ProductRepository())
    }
    private val providerViewModel: ProviderViewModel by viewModels{
        ProviderViewModelFactory(ProviderRepository())
    }

    private var bottomNav: BottomNavigationView? = null


    private var maxOrders = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuMainBinding.inflate(inflater,container,false)
        bottomNav = binding.root.findViewById(R.id.bnvMenu)
        initListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        handleOrdersVisibilityStates(isLoading = true, isEmpty = true)
        handleProductsVisibilityStates(isLoading = true, isEmpty = true)
        handleProvidersVisibilityStates(isLoading = true, isEmpty = true)

        observeLoadingState()
        observeLoadingStateProducts()
        observeLoadingStateProviders()

        observeOrders()
        observeProducts()
        observeProviders()

        refreshMenu()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            emptyStateContainer = binding.emptyStateOrders.root,
            recyclerView = binding.rvOrders,
            loadingProgressBar = binding.loadingProgressBar,
            maxItems = maxOrders
        )
        binding.rvOrders.apply{
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        productLowAdapter = ProductLowAdapter(
            emptyStateContainer = binding.emptyStateLowStock.root,
            recyclerView = binding.rvLowStockProducts,
            loadingProgressBar = binding.loadingProgressBarLowStock
        )
        binding.rvLowStockProducts.apply {
            adapter = productLowAdapter
            layoutManager = LinearLayoutManager(context)
        }

        providerMenuAdapter = ProvidersMenuAdapter(
            emptyStateContainer = binding.emptyStateProviders.root,
            recyclerView = binding.rvProvidersMenu,
            loadingProgressBar = binding.loadingProgressBarProviders
        )
        binding.rvProvidersMenu.apply{
            adapter = providerMenuAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeLoadingState() {
        orderViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            handleOrdersVisibilityStates(isLoading, orderViewModel.ordersWithProducts.value?.isEmpty() ?: true)
        }

    }
    private fun observeLoadingStateProducts() {
        productViewModel.isLoadingProducts.observe(viewLifecycleOwner) { isLoading ->
            val isEmpty = productViewModel.products.value?.none { it.stock <= 5 } ?: true
            handleProductsVisibilityStates(isLoading, isEmpty)
        }
    }

    private fun observeOrders() {
        orderViewModel.ordersWithProducts.observe(viewLifecycleOwner) { orders ->
            val filteredOrders = orders.filter { it.order.estado.lowercase() == "pendiente" }
                .take(maxOrders)
            orderAdapter.updateItems(filteredOrders)
            handleOrdersVisibilityStates(
                isLoading = false,
                isEmpty = filteredOrders.isEmpty()
            )
        }
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            productLowAdapter.updateItems(products ?: emptyList()) {
                val isLowStock = it.stock <= 5
                isLowStock
            }
        }
    }

    private fun observeProviders() {
        providerViewModel.providers.observe(viewLifecycleOwner) { providers ->
            providerMenuAdapter.updateItems(providers)
            handleProvidersVisibilityStates(
                isLoading = false,
                isEmpty = providers.isEmpty()
            )
        }
    }

    private fun observeLoadingStateProviders() {
        providerViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val isEmpty = providerViewModel.providers.value?.isEmpty() ?: true
            handleProvidersVisibilityStates(isLoading, isEmpty)
        }
    }

    private fun handleProductsVisibilityStates(isLoading: Boolean, isEmpty: Boolean) {
        binding.apply {
            loadingProgressBarLowStock.visibility = if (isLoading) View.VISIBLE else View.GONE

            lowStockContainer.visibility = View.VISIBLE

            if (!isLoading) {
                rvLowStockProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
                emptyStateLowStock.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                emptyStateLowStock.tvEmptyState.apply {
                    text = "No hay productos con stock bajo"
                    visibility = if (isEmpty) View.VISIBLE else View.GONE
                }
            } else {
                rvLowStockProducts.visibility = View.GONE
                emptyStateLowStock.root.visibility = View.GONE
            }
        }
    }

    private fun handleOrdersVisibilityStates(isLoading: Boolean, isEmpty: Boolean) {
        binding.apply {
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            if (!isLoading) {
                rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
                emptyStateOrders.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                emptyStateOrders.tvEmptyState.apply {
                    text = "No hay pedidos pendientes"
                    visibility = if (isEmpty) View.VISIBLE else View.GONE
                }
            } else {
                rvOrders.visibility = View.GONE
                emptyStateOrders.root.visibility = View.GONE
            }
        }
    }

    private fun handleProvidersVisibilityStates(isLoading: Boolean, isEmpty: Boolean) {
        binding.apply {
            loadingProgressBarProviders.visibility = if (isLoading) View.VISIBLE else View.GONE

            providersContainer.visibility = View.VISIBLE

            if (!isLoading) {
                rvProvidersMenu.visibility = if (isEmpty) View.GONE else View.VISIBLE
                emptyStateProviders.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                emptyStateProviders.tvEmptyState.apply {
                    text = "No hay proveedores disponibles"
                    visibility = if (isEmpty) View.VISIBLE else View.GONE
                }
            } else {
                rvProvidersMenu.visibility = View.GONE
                emptyStateProviders.root.visibility = View.GONE
            }
        }
    }

    private fun initListeners() {
        val btnDrawerToggle = binding.root.findViewById<ImageButton>(R.id.btnDrawerToggle)
        val drawerlt = binding.root.findViewById<DrawerLayout>(R.id.drawerlt)
        btnDrawerToggle.setOnClickListener {
            drawerlt.openDrawer(GravityCompat.START)
        }

        binding.btnMoreOrders.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(OrdersFragment(), "Pedidos")
        }

        binding.btnMoreProviders.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(ProviderFragment(), "Proveedores")
        }

        binding.btnMoreProducts.setOnClickListener {
            val fragment = InventoryMenuFragment().apply {
                arguments = Bundle().apply {
                    putInt("category_id", 0)
                    putString("category_name", "Todo")
                }
            }
            (activity as? MainMenu)?.replaceFragment(fragment, "Inventario")
        }
    }

    private fun refreshMenu() {
        orderViewModel.loadOrders()
        productViewModel.loadProducts()
        providerViewModel.loadProviders()
    }

    override fun onResume() {
        super.onResume()
        refreshMenu()
    }

}