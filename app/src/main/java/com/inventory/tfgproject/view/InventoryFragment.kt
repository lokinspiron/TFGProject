package com.inventory.tfgproject.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

    private lateinit var imgMoreContent : ImageView

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close_anim
        )
    }

    private var clicked = false


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
                    InventoryMenuFragment.newInstanceForCategory(category.id, category.name)
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

        initVisibility()
        initListeners()
        initViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initVisibility(){
        binding.pbInventory.visibility = View.VISIBLE
        binding.rvCategories.visibility = View.GONE
        binding.divider.visibility = View.GONE
        binding.btnAddCategory.visibility = View.GONE
        binding.lltInventory.visibility = View.GONE
    }

    private fun initListeners() {
        binding.btnAddCategory.setOnClickListener {
            val dialogFragment: DialogFragment = DialogCreateCategoryFragment()
            dialogFragment.show(childFragmentManager, "CreateCategory")
        }

        requireActivity().findViewById<ImageView>(R.id.btnCategoryAction)?.setOnClickListener {
            onAddButtonClicked()
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
            binding.pbInventory.visibility = View.GONE
            binding.divider.visibility = View.VISIBLE
            binding.rvCategories.visibility = View.VISIBLE
            binding.btnAddCategory.visibility = View.VISIBLE

            if (categories.size > 1) {
                binding.lltInventory.visibility = View.GONE
            } else {
                binding.lltInventory.visibility = View.VISIBLE
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
    private fun onAddButtonClicked(){
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }
    private fun setAnimation(clicked: Boolean) {
        val imgMoreContent = requireActivity().findViewById<ImageView>(R.id.btnCategoryAction)
        imgMoreContent?.let {
            if (!clicked) {
                it.startAnimation(rotateOpen)
            } else {
                it.startAnimation(rotateClose)
            }
        }
    }

    private fun setClickable(clicked: Boolean) {
        val imgMoreContent = requireActivity().findViewById<ImageView>(R.id.btnCategoryAction)
        imgMoreContent?.isClickable = !clicked
    }
}