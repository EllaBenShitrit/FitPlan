package com.ellabs.fitplan.managers

import android.content.Context


fun saveWeights(context: Context, weights: Map<String, Float>) {
    val prefs = context.getSharedPreferences("weights_prefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    for ((key, value) in weights) {
        editor.putFloat(key, value)
    }
    editor.apply()
}

fun loadWeights(context: Context): MutableMap<String, Float> {
    val prefs = context.getSharedPreferences("weights_prefs", Context.MODE_PRIVATE)
    val allEntries = prefs.all
    val map = mutableMapOf<String, Float>()
    for ((key, value) in allEntries) {
        if (value is Float) {
            map[key] = value
        }
    }
    return map
}