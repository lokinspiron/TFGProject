package com.inventory.tfgproject

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
object AnimationUtil {
    var isAnimationDone: Boolean = false

    fun upAnimation(context: Context, view1: View, anim1ResId: Int,) {
        val anim1: Animation = AnimationUtils.loadAnimation(context,anim1ResId)

        view1.animation = anim1
    }

    fun downAnimation(context:Context, view2:View, anim2ResId:Int){
        val anim2: Animation = AnimationUtils.loadAnimation(context,anim2ResId)

        view2.animation = anim2

    }
}