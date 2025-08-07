package com.ellabs.fitplan.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.classes.Exercise
import com.ellabs.fitplan.managers.saveWeights

class SimpleExercisesAdapter(
    private val exercises: List<Exercise>,
    private val exerciseWeights: MutableMap<String, Float>,
    private val listener: OnExerciseClickListener? = null
) : RecyclerView.Adapter<SimpleExercisesAdapter.SimpleExerciseViewHolder>() {

    interface OnExerciseClickListener {
        fun onExerciseClicked(exercise: Exercise)
    }

    inner class SimpleExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvExerciseNameOnly)
        val etWeight: EditText = itemView.findViewById(R.id.etExerciseWeight)

        init {
            itemView.setOnClickListener {
                val exercise = exercises[adapterPosition]
                listener?.onExerciseClicked(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_exercise, parent, false)
        return SimpleExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvName.text = exercise.name

        val weight = exerciseWeights[exercise.name] ?: 0f
        holder.etWeight.setText(weight.toString())

        holder.etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newWeight = s?.toString()?.toFloatOrNull() ?: 0f
                exerciseWeights[exercise.name] = newWeight
                saveWeights(holder.itemView.context, exerciseWeights)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = exercises.size
}


