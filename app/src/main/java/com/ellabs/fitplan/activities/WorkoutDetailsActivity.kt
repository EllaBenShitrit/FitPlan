package com.ellabs.fitplan.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.adapters.SimpleExercisesAdapter
import com.ellabs.fitplan.classes.Exercise
import com.ellabs.fitplan.classes.Workout
import com.ellabs.fitplan.managers.CalendarHelper
import com.ellabs.fitplan.managers.loadWeights
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class WorkoutDetailsActivity : AppCompatActivity(), SimpleExercisesAdapter.OnExerciseClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var exercisesAdapter: SimpleExercisesAdapter
    private val exercisesList = mutableListOf<Exercise>()
    private lateinit var btnBack: Button
    private lateinit var tvWorkoutTitle: TextView
    private lateinit var btnDeleteWorkout: Button
    private lateinit var currentWorkout: Workout
    private lateinit var btnAddWorkoutToCalendar: Button
    private lateinit var switchIsPublic: SwitchCompat
    private lateinit var switchLabel: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_details)
        btnBack = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewExercises)
        tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle)
        btnDeleteWorkout = findViewById(R.id.btnDeleteWorkout)
        btnAddWorkoutToCalendar = findViewById(R.id.btnAddWorkoutToCalendar)
        switchIsPublic = findViewById<SwitchCompat>(R.id.switch_is_public)
        switchLabel = findViewById<TextView>(R.id.tvSwitchLabel)
        val calendarHelper = CalendarHelper(this)

        btnAddWorkoutToCalendar.setOnClickListener {
            calendarHelper.addWorkoutToDeviceCalendar()
        }

        val exerciseWeights = loadWeights(this)

        exercisesAdapter = SimpleExercisesAdapter(exercisesList, exerciseWeights, object : SimpleExercisesAdapter.OnExerciseClickListener {
            override fun onExerciseClicked(exercise: Exercise) {
                showExerciseDialog(exercise)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = exercisesAdapter

        // Getting workout object from the Intent
        val workout = intent.getParcelableExtra<Workout>("workout")
        Log.d("WorkoutDetails", "Received workout: $workout")

        if (workout != null) {
            currentWorkout = workout
            tvWorkoutTitle.text = workout.title
            exercisesList.addAll(workout.exercises)
            exercisesAdapter.notifyDataSetChanged()

            fetchWorkoutFromFirestore(workout.id)
            setupPublicSwitchListener()
        }

        btnBack.setOnClickListener {
            finish() // Return to MyWorkoutPlanActivity
        }

        btnDeleteWorkout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete workout?")
                .setMessage("Are you sure you want to delete your workout?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteWorkoutFromFirestore()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
       }
    }

    override fun onExerciseClicked(exercise: Exercise) {
        showExerciseDialog(exercise)
    }

    private fun showExerciseDialog(exercise: Exercise) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_exercise_details, null)

        dialogView.findViewById<TextView>(R.id.tvDialogExerciseName).text = exercise.name
        dialogView.findViewById<TextView>(R.id.tvDialogExerciseDescription).text = exercise.description

        val imageView = dialogView.findViewById<ImageView>(R.id.ivDialogExerciseImage)
        imageView.setImageResource(exercise.imageResId)
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun deleteWorkoutFromFirestore() {
        val userId =  currentWorkout.userId
        val workoutId = currentWorkout.id

        if (userId.isNullOrEmpty() || workoutId.isEmpty()) {
            Toast.makeText(this, "Cannot delete workout.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("workouts")
            .document(workoutId)
            .delete()
            .addOnSuccessListener {
                Log.e("WorkoutDelete", "Workout deleted from user collection")
                db.collection("workouts")
                    .document(workoutId)
                    .delete()
                    .addOnSuccessListener {
                        Log.e("WorkoutDelete", "Workout also deleted from public collection")
                    }
                    .addOnFailureListener { e ->
                        Log.e("WorkoutDelete", "Failed to delete workout from public collection", e)
                    }
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("WorkoutDelete", "Failed to delete workout", e)
            }
    }

    private fun fetchWorkoutFromFirestore(workoutId: String) {
        val userId = currentWorkout.userId
        if (userId.isNullOrEmpty()) {
            Log.e("WorkoutDetails", "Workout is missing userId")
            return
        }
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("workouts")
            .document(workoutId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isPublic = document.getBoolean("isPublic") ?: false
                    switchIsPublic.setOnCheckedChangeListener(null) // ניתוק זמני
                    switchIsPublic.isChecked = isPublic
                    switchLabel.text = if (isPublic) {
                        "Make your\nworkout private"
                    } else {
                        "Make your\nworkout public"
                    }
                    currentWorkout = currentWorkout.copy(isPublic = isPublic)
                    setupPublicSwitchListener()

                }
            }
            .addOnFailureListener {
                Log.e("WorkoutDetails", "Failed to fetch workout data", it)
            }
    }


    private fun setupPublicSwitchListener() {
        Log.d("SwitchDebug", "userId = ${currentWorkout.userId}, workoutId = ${currentWorkout.id}")

        switchIsPublic.isChecked = currentWorkout.isPublic
        switchLabel.text = if (currentWorkout.isPublic) {
            "Make your\nworkout private"
        } else {
            "Make your\nworkout public"
        }

        switchIsPublic.setOnCheckedChangeListener { _, isChecked ->
            val userId = currentWorkout.userId
            val workoutId = currentWorkout.id

            if (userId.isNullOrEmpty() || workoutId.isEmpty()) {
                Toast.makeText(this, "Cannot update workout status", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            val db = FirebaseFirestore.getInstance()

            // Update isPublic in user's private collection
            db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(workoutId)
                .update("isPublic", isChecked)
                .addOnSuccessListener {
                    val msg = if (isChecked) "Workout is now public" else "Workout is now private"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("WorkoutUpdate", "Failed to update isPublic in user's collection", e)
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }

            // Update or delete in public collection
            val publicRef = db.collection("workouts").document(workoutId)

            if (isChecked) {
                // Make public: create or update in public workouts collection
                val publicWorkoutMap = hashMapOf(
                    "title" to currentWorkout.title,
                    "exercises" to currentWorkout.exercises.map {
                        mapOf(
                            "name" to it.name,
                            "description" to it.description,
                            "imageName" to resources.getResourceEntryName(it.imageResId)
                        )
                    },
                    "userId" to userId,
                    "isPublic" to true
                )

                publicRef.set(publicWorkoutMap)
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update public collection", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Make private: remove from public workouts collection
                publicRef.delete()
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to remove from public collection", Toast.LENGTH_SHORT).show()
                    }
            }

            // Update the switch label
            switchLabel.text = if (isChecked) {
                "Make your\nworkout private"
            } else {
                "Make your\nworkout public"
            }
        }
    }

}