package com.example.tfgproject

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.Toast


fun Activity.toast(text: String,length: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this,text,length).show()
}

fun Activity.span(unselectedPart: String, selectedPart: String): SpannableStringBuilder {
    val context: Context = this
    val completedText = SpannableStringBuilder("$unselectedPart $selectedPart")

    completedText.apply{
        setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            0,
            unselectedPart.length,
            (unselectedPart + selectedPart).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}