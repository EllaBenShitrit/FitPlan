package com.ellabs.fitplan.classes

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Exercise(
    val name: String,
    val imageResId: Int,
    val description: String,
) : Parcelable