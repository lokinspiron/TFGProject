package com.inventory.tfgproject.extension

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

fun DialogFragment.show(launcher:DialogFragment,activity:FragmentActivity){
    launcher.show(this,activity)
}