package com.ellabs.fitplan.adapters
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ellabs.fitplan.classes.Workout
import com.ellabs.fitplan.R
import android.view.LayoutInflater

import androidx.recyclerview.widget.RecyclerView

class WorkoutsAdapter(
    private val workouts: List<Workout>, // Workouts list to present
    private val listener: OnWorkoutClickListener // listener for clicking on some workout from the list
) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {

    interface OnWorkoutClickListener {
        fun onWorkoutClicked(workout: Workout) // Will be activated when clicking on a workout
    }

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.tvWorkoutTitle)

        init {
            itemView.setOnClickListener {
                val workout = workouts[adapterPosition]
                listener.onWorkoutClicked(workout)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false) // Creating the layout of workout line
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.workoutName.text = workout.title
    }

    override fun getItemCount(): Int = workouts.size
}