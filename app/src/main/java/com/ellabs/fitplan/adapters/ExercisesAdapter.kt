package com.ellabs.fitplan.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.classes.Exercise
import com.ellabs.fitplan.R

class ExercisesAdapter(
    private val exercises: List<Exercise>,
    private val listener: OnExerciseClickListener
) : RecyclerView.Adapter<ExercisesAdapter.ExerciseViewHolder>() {

    interface OnExerciseClickListener {
        fun onExerciseClicked(exercise: Exercise)
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val ivImage: ImageView = itemView.findViewById(R.id.ivExerciseImage)
        val tvDescription: TextView = itemView.findViewById(R.id.tvExerciseDescription)

        init {
            itemView.setOnClickListener {
                val exercise = exercises[adapterPosition]
                listener.onExerciseClicked(exercise)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvName.text = exercise.name
        holder.ivImage.setImageResource(exercise.imageResId)
        holder.tvDescription.text = exercise.description
    }

    override fun getItemCount(): Int = exercises.size
}