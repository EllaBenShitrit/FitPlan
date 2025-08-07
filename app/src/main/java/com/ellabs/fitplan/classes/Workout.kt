package com.ellabs.fitplan.classes

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Workout(
    val title: String,
    val exercises: List<Exercise>,
    val id: String = "", // ID is a unique identifier for deleting the workout
    val isPublic: Boolean = false,
    val userId: String = ""

) : Parcelable