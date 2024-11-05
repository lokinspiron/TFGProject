package com.example.tfgproject

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
object AnimationUtil {
    fun UpAnimation(context: Context, view1: View ,anim1ResId: Int,) {
        val anim1: Animation = AnimationUtils.loadAnimation(context,anim1ResId)

        view1.animation = anim1
    }

    fun DownAnimation(context:Context,view2:View,anim2ResId:Int){
        val anim2: Animation = AnimationUtils.loadAnimation(context,anim2ResId)

        view2.animation = anim2

    }
}