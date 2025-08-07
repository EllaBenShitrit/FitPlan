package com.ellabs.fitplan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.classes.Exercise

class ExerciseSelectionAdapter(
    private val exercises: List<Exercise>,
    private val selectedExercises: MutableList<Exercise>
) : RecyclerView.Adapter<ExerciseSelectionAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxExerciseName) // Show exercise name with a checkbox
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_checkbox, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.checkBox.text = exercise.name
        holder.checkBox.isChecked = selectedExercises.contains(exercise)

        // When clicking on checkbox - Add/remove to/from chosen exercises list
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedExercises.add(exercise)
            else selectedExercises.remove(exercise)
        }

        // With long click on name of exercise - show a dialog screen with image + description
        holder.itemView.setOnLongClickListener {
            showExerciseDetailsDialog(holder.itemView.context, exercise)
            true
        }
    }

    override fun getItemCount(): Int = exercises.size

    private fun showExerciseDetailsDialog(context: Context, exercise: Exercise) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exercise_details, null)
        dialogView.findViewById<TextView>(R.id.tvDialogExerciseName).text = exercise.name
        dialogView.findViewById<TextView>(R.id.tvDialogExerciseDescription).text = exercise.description
        dialogView.findViewById<ImageView>(R.id.ivDialogExerciseImage).setImageResource(exercise.imageResId)

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }
}