package com.inventory.tfgproject.view

import android.app.AlertDialog
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
import androidx.transition.Visibility
import com.inventory.tfgproject.CategoryAdapter
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.DialogAddSubcategoryBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.viewmodel.CategoryViewModel
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Subcategory
import java.util.UUID


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
        categoryAdapter = CategoryAdapter(
            mutableListOf(),
            { category ->
                // Original category click logic
                Log.d("CategoryClick", "Clicked category: ${category.name}")
                toast("Clicked: ${category.name}", Toast.LENGTH_SHORT)
                (activity as? MainMenu)?.replaceFragment(InventoryMenuFragment.newInstance(category.id, category.name))
            },
            { category ->
                // Add subcategory logic
                // Open a dialog to add a subcategory to this specific category
                showAddSubcategoryDialog(category)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        recyclerView.adapter = categoryAdapter
        binding.progressBar.visibility = View.VISIBLE
        binding.lltInventory.visibility = View.GONE

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


    private fun initViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            categoryAdapter.updateCategories(categories)
            binding.progressBar.visibility = View.GONE
            binding.lltInventory.visibility = View.VISIBLE

            if(categories.size > 1){
                binding.imgNoContent.visibility = View.GONE
                binding.txtNoContent.visibility = View.GONE
                binding.txtAddCategories.visibility = View.GONE
            } else {
                binding.imgNoContent.visibility = View.VISIBLE
                binding.txtNoContent.visibility = View.VISIBLE
                binding.txtAddCategories.visibility = View.VISIBLE
            }
        })
        categoryViewModel.fetchCategories()
    }

    private fun showAddSubcategoryDialog(category: Category) {
        val dialogBinding = DialogAddSubcategoryBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnAddSubcategory.setOnClickListener {
            val subcategoryName = dialogBinding.edtSubcategoryName.text.toString().trim()
            if (subcategoryName.isNotEmpty()) {
                val subcategory = Subcategory(
                    id = UUID.randomUUID().toString(),
                    name = subcategoryName
                )
                categoryViewModel.addSubcategoryToCategory(category, subcategory)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}