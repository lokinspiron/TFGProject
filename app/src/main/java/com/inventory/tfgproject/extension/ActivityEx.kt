package com.inventory.tfgproject.extension

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.inventory.tfgproject.R


fun Activity.toast(text: String,length: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this,text,length).show()
}

fun Fragment.toast(text: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(),text,length).show()
}

fun Activity.span(unselectedPart: String, selectedPart: String): SpannableStringBuilder {
    val context: Context = this
    val completedText = SpannableStringBuilder("$unselectedPart $selectedPart")

    val start = unselectedPart.length + 1
    val end = start + selectedPart.length

    completedText.apply {
        setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setSpan(
            android.text.style.ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return completedText
}