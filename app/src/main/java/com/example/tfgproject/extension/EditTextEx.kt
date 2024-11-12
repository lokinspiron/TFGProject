package com.example.tfgproject.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.onTextChanged(listener:(String)-> Unit){
    this.addTextChangedListener(object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            listener(s.toString())
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    })
}

fun EditText.loseFocusAfterAction(action:Int){
    this.setOnEditorActionListener { v, actionId, _ ->
        if (actionId == action) {
            this.dismissKeyboard()
            v.clearFocus()
        }
        return@setOnEditorActionListener false
    }
}

