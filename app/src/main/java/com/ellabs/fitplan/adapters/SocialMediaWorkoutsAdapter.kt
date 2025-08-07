package com.ellabs.fitplan.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.classes.UserProfile
import com.ellabs.fitplan.classes.Workout
import com.ellabs.fitplan.R
import android.view.LayoutInflater
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import androidx.appcompat.app.AlertDialog


class SocialMediaWorkoutsAdapter(
    private val workouts: List<Pair<Workout, UserProfile>>,
    private val hideSaveButton: Boolean = false,
    private val showDeleteButtonInsteadOfSave: Boolean = false,
    private val onDeleteClick: ((Workout) -> Unit)? = null
) : RecyclerView.Adapter<SocialMediaWorkoutsAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWorkoutTitle: TextView = itemView.findViewById(R.id.tvWorkoutTitle)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val btnSaveWorkout: Button = itemView.findViewById(R.id.btnSaveWorkout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_social_media_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val (workout, user) = workouts[position]

        holder.tvWorkoutTitle.text = workout.title
        holder.tvUsername.text = user.username

        // When clicking on workout name - a dialog with workout details is opened
        holder.tvWorkoutTitle.setOnClickListener {
            showWorkoutExercisesDialog(holder.itemView.context, workout)
        }

        // When clicking on username - a dialog with user profile details is opened
        holder.tvUsername.setOnClickListener {
            showUserProfileDialog(holder.itemView.context, user)
        }


        // If we are on SocialMediaActivity- it will be a "Save Workout" button. The workout will be saved to SocialMediaSavedWorkoutsActivity
        // If we are on SocialMediaSavedWorkoutsActivity- it will be a "Delete Workout" button.
        when {
            hideSaveButton -> {
                holder.btnSaveWorkout.visibility = View.GONE
            }
            showDeleteButtonInsteadOfSave -> {
                holder.btnSaveWorkout.visibility = View.VISIBLE
                holder.btnSaveWorkout.text = "Delete Workout"
                holder.btnSaveWorkout.setOnClickListener {
                    onDeleteClick?.invoke(workout)
                }
            }
            else -> {
                holder.btnSaveWorkout.visibility = View.VISIBLE
                holder.btnSaveWorkout.text = "Save Workout"
                holder.btnSaveWorkout.setOnClickListener {
                    saveWorkoutToSavedList(holder.itemView.context, workout)
                }
            }
        }
    }

    override fun getItemCount(): Int = workouts.size

    private fun showUserProfileDialog(context: Context, user: UserProfile) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_user_profile, null)

        dialogView.findViewById<TextView>(R.id.tvUsername).text = user.username
        dialogView.findViewById<TextView>(R.id.tvStatus).text = user.status
        dialogView.findViewById<TextView>(R.id.tvBio).text = user.bio
        val ivProfile = dialogView.findViewById<ImageView>(R.id.ivProfilePicture)

        //Load profile image from Firebase Storage
        if (user.imageUrl.isNotEmpty()) {
            Glide.with(context).load(user.imageUrl).into(ivProfile)
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun saveWorkoutToSavedList(context: Context, workout: Workout) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Saving user details & his workout details
        val userProfile = workouts.find { it.first.id == workout.id }?.second ?: return
        val workoutMap = hashMapOf(
            "title" to workout.title,
            "exercises" to workout.exercises.map {
                mapOf(
                    "name" to it.name,
                    "description" to it.description,
                    "imageName" to context.resources.getResourceEntryName(it.imageResId)
                )
            },
            "username" to userProfile.username,
            "status" to userProfile.status,
            "bio" to userProfile.bio,
            "imageUrl" to userProfile.imageUrl
        )

        db.collection("users")
            .document(userId)
            .collection("savedWorkouts") // New collection in Firebase Firestore
            .document(workout.id)
            .set(workoutMap)
            .addOnSuccessListener {
                Log.d("SocialMediaWorkoutSave", "Workout saved: $workoutMap")
                Toast.makeText(context, "Workout saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.e("SocialMediaWorkoutSave", "Failed to save workout: $workoutMap", e)
                Toast.makeText(context, "Failed to save workout", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showWorkoutExercisesDialog(context: Context, workout: Workout) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_social_media_workout, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.exercisesContainer)

        for (exercise in workout.exercises) {
            val exerciseView = LayoutInflater.from(context).inflate(R.layout.item_exercise, container, false)

            exerciseView.findViewById<TextView>(R.id.tvExerciseName).text = exercise.name
            exerciseView.findViewById<TextView>(R.id.tvExerciseDescription).text = exercise.description
            exerciseView.findViewById<ImageView>(R.id.ivExerciseImage).setImageResource(exercise.imageResId)

            container.addView(exerciseView)
        }

        AlertDialog.Builder(context)
            .setTitle(workout.title)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }
}