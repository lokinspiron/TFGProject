package com.inventory.tfgproject.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.CategoryAdapter
import com.inventory.tfgproject.R
import com.inventory.tfgproject.SubcategoryAdapter
import com.inventory.tfgproject.databinding.DialogAddSubcategoryBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.viewmodel.CategoryViewModel
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Subcategory
import java.util.UUID


class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private lateinit var subcategoryRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.rvCategories
        categoryAdapter = CategoryAdapter(
            mutableListOf(),
            { category ->
                Log.d("CategoryClick", "Clicked category: ${category.name}")
                categoryViewModel.selectCategory(category)

                (activity as? MainMenu)?.replaceFragment(
                    InventoryMenuFragment.newInstanceForCategory(category.id, category.name),
                    category.name
                )
            },
            { category ->
                showAddSubcategoryDialog(category)
            },
            { subcategory ->
                Log.d("SubcategoryClick", "Clicked subcategory: ${subcategory.name}, ID: ${subcategory.id}")

                (activity as? MainMenu)?.replaceFragment(
                    InventoryMenuFragment.newInstanceForSubcategory(subcategory.id, subcategory.name),"${subcategory.name}"
                )
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = categoryAdapter

        view.post {
            val rvSubcategories = view.findViewById<RecyclerView>(R.id.rvSubcategories)

            if (rvSubcategories == null) {
                Log.e("SetupSubcategoryRecycler", "RecyclerView is null. Cannot setup RecyclerView.")
                return@post
            }

            subcategoryRecyclerView = rvSubcategories
            subcategoryAdapter = SubcategoryAdapter(
                mutableListOf()
            ) { subcategory ->
                Log.d("SubcategoryClick", "Subcategory clicked: ${subcategory.name}, ID: ${subcategory.id}")

                (activity as? MainMenu)?.let { mainMenu ->
                    val fragment = InventoryMenuFragment.newInstanceForSubcategory(subcategory.id, subcategory.name)
                    mainMenu.replaceFragment(fragment)
                } ?: Log.e("SubcategoryClick", "MainMenu activity is null")
            }

            subcategoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            subcategoryRecyclerView.adapter = subcategoryAdapter

            Log.d("SetupSubcategoryRecycler", "RecyclerView setup completed")
        }

        initListeners()
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

        view?.findViewById<ImageView>(R.id.btnCategoryAction)?.setOnClickListener {
            if (subcategoryRecyclerView.visibility == View.VISIBLE) {
                subcategoryRecyclerView.visibility = View.GONE
            } else {
                subcategoryRecyclerView.visibility = View.VISIBLE
            }
        }
    }


    private fun initViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            categoryAdapter.updateCategories(categories)
            binding.progressBar.visibility = View.GONE
            binding.lltInventory.visibility = View.VISIBLE

            if (categories.size > 1) {
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