package com.inventory.tfgproject.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.CategoryAdapter
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.viewmodel.CategoryViewModel
import com.inventory.tfgproject.extension.toast


class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel : CategoryViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

        recyclerView = view.findViewById(R.id.rvCategories)
        categoryAdapter = CategoryAdapter(mutableListOf()) { category ->
            Log.d("CategoryClick", "Clicked category: ${category.name}")
            toast("Clicked: ${category.name}",Toast.LENGTH_SHORT)

            (activity as? MainMenu)?.replaceFragment(InventoryMenuFragment.newInstance(category.id, category.name))
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = categoryAdapter
        initViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListeners() {
        binding.btnAddCategory.setOnClickListener {
            val dialogFragment: DialogFragment = DialogCreateCategoryFragment()
            dialogFragment.show(childFragmentManager, "CreateCategory")
        }
    }


    private fun initViewModel(){
        categoryViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            Log.d("InventoryFragment", "Observed categories: $categories")
            if (categories != null) {
                categoryAdapter.updateCategories(categories)
            }
        })
        categoryViewModel.fetchCategories()
    }
}