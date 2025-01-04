package com.inventory.tfgproject.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.repository.OrderRepository
import com.inventory.tfgproject.adapter.OrderViewAdapter
import com.inventory.tfgproject.modelFactory.OrderViewModelFactory
import com.inventory.tfgproject.databinding.FragmentOrdersBinding
import com.inventory.tfgproject.viewmodel.OrderViewModel

class OrdersFragment : Fragment() {

    private lateinit var binding : FragmentOrdersBinding
    private lateinit var orderViewAdapter: OrderViewAdapter
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentOrdersBinding.inflate(inflater,container,false)
        setupRecyclerView()
        setupObservers()
        initVisibility()
        return binding.root
    }

    private fun initVisibility() {
        binding.apply {
            rvViewOrder.visibility = View.GONE
            imgNoContent.visibility = View.GONE
            tvNoOrders.visibility = View.GONE
            pbOrders.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        orderViewAdapter = OrderViewAdapter(
            onQuantityChanged = { orderWithProduct, newQuantity ->
                orderViewModel.updateOrderWithProductQuantity(orderWithProduct, newQuantity)
            },
            onStateChanged = { orderWithProduct, newState ->
                orderViewModel.updateOrderState(orderWithProduct, newState)
            }
        )

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val orderToDelete = orderViewAdapter.getOrderAt(position)

                val dialogFragment = DialogSafeChangeFragment.newInstance(
                    "Estas a punto de eliminar un pedido",
                    "Eliminar"
                )

                dialogFragment.setOnPositiveClickListener {
                    orderViewModel.deleteOrder(orderToDelete)
                }

                dialogFragment.setOnDismissListener {
                    orderViewAdapter.notifyItemChanged(position)
                }

                dialogFragment.show(parentFragmentManager, "DeleteOrderDialog")
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                val deleteText = "Eliminar"
                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }

                background.draw(c)

                if (dX > 0) {
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    background.draw(c)

                    val textY = itemView.top + ((itemView.bottom - itemView.top) / 2.0f) + (textPaint.textSize / 2)
                    c.drawText(
                        deleteText,
                        itemView.left + dX / 2,
                        textY,
                        textPaint
                    )
                } else if (dX < 0) {
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    val textY = itemView.top + ((itemView.bottom - itemView.top) / 2.0f) + (textPaint.textSize / 2)
                    c.drawText(
                        deleteText,
                        itemView.right + dX / 2,
                        textY,
                        textPaint
                    )
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        binding.rvViewOrder.apply {
            adapter = orderViewAdapter
            layoutManager = LinearLayoutManager(context)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun setupObservers() {
        orderViewModel.ordersWithProducts.observe(viewLifecycleOwner) { orders ->
            orderViewAdapter.updateOrders(orders)
            binding.apply {
                if (orders.isEmpty()) {
                    rvViewOrder.visibility = View.GONE
                    imgNoContent.visibility = View.VISIBLE
                    tvNoOrders.visibility = View.VISIBLE
                } else {
                    rvViewOrder.visibility = View.VISIBLE
                    imgNoContent.visibility = View.GONE
                    tvNoOrders.visibility = View.GONE
                }
                pbOrders.visibility = View.GONE
            }
        }
        orderViewModel.loadOrders()
    }
}