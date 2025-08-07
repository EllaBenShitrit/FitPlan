package com.ellabs.fitplan.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.adapters.SimpleExercisesAdapter
import com.ellabs.fitplan.classes.Exercise
import com.ellabs.fitplan.classes.Workout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ellabs.fitplan.managers.Constants
import com.ellabs.fitplan.managers.loadWeights
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson


class CreateWorkoutActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnLowerBody: Button
    private lateinit var btnUpperBody: Button
    private lateinit var btnCardio: Button
    private lateinit var etWorkoutName: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveButton: Button
    private val selectedExercises = mutableListOf<Exercise>()
    private lateinit var adapter: SimpleExercisesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_workout)

        etWorkoutName = findViewById(R.id.etWorkoutName)
        recyclerView = findViewById(R.id.recyclerViewSelectedExercises)
        saveButton = findViewById(R.id.btnSaveWorkout)
        btnBack = findViewById(R.id.btnBack)
        btnLowerBody = findViewById(R.id.btnLowerBody)
        btnUpperBody = findViewById(R.id.btnUpperBody)
        btnCardio = findViewById(R.id.btnCardio)

        // Set up RecyclerView with adapter that shows only exercise names & weights
        val exerciseWeights = loadWeights(this)
        adapter = SimpleExercisesAdapter(selectedExercises, exerciseWeights)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnLowerBody.setOnClickListener {
            openExerciseSelection("lower_body")
        }

        btnUpperBody.setOnClickListener {
            openExerciseSelection("upper_body")
        }

        btnCardio.setOnClickListener {
            openExerciseSelection("cardio")
        }

        // Save workout
        saveButton.setOnClickListener {
            val name = etWorkoutName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Workout name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedExercises.isEmpty()) {
                Toast.makeText(this, "Please add at least one exercise", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val workout = Workout(
                title = name,
                exercises = selectedExercises.toList(),
                userId = userId
            )
            saveWorkoutToFirestore(workout) // Save workout also to Firestore
        }

        btnBack.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Discard workout?")
                .setMessage("You have unsaved changes. Are you sure you want to go back?")
                .setPositiveButton("Yes") { _, _ -> finish() } // Return to MyWorkoutPlanActivity screen without saving anything
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Helper to open ExerciseSelectionActivity with category
    private fun openExerciseSelection(category: String) {
        val intent = Intent(this, ExerciseSelectionActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("selectedExercises", ArrayList(selectedExercises)) // pass current selection
        startActivityForResult(intent, Constants.REQUEST_EXERCISES)
    }

    // Handle returned selected exercises from ExerciseSelectionActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DEBUG_FLOW", "onActivityResult called")

        if (requestCode == Constants.REQUEST_EXERCISES && resultCode == RESULT_OK) {
            // Get the JSON string from the intent
            Log.d("DEBUG_FLOW", "onActivityResult called")
            val json = data?.getStringExtra(Constants.EXTRA_SELECTED_EXERCISES)
            Log.d("DEBUG_FLOW", "Received JSON: $json")
            if (json != null) {
                val type = object : TypeToken<List<Exercise>>() {}.type
                val returnedExercises: List<Exercise> = Gson().fromJson(json, type)
                for (exercise in returnedExercises) {
                    Log.d("DEBUG_FLOW", "Exercise: ${exercise.name}")
                    if (!selectedExercises.contains(exercise)) {
                        selectedExercises.add(exercise)  // If an exercise doesn't exist on the list - adding it to the list

                    }
                }
                adapter.notifyDataSetChanged()  // Update the RecyclerView to present the new list
                Log.d("DEBUG_FLOW", "Adapter updated successfully")

            }
        }
    }


    // Save the workout to Firestore under the current user
    private fun saveWorkoutToFirestore(workout: Workout) {
        Log.d("WorkoutSave", "Entered function saveWorkoutToFirestore")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("WorkoutSave", "UserID is: $userId")
        if (userId == null) {
            Log.e("WorkoutSave", "User not logged in")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val workoutMap = hashMapOf(
            "title" to workout.title,
            "exercises" to workout.exercises.map {
                mapOf(
                    "name" to it.name,
                    "description" to it.description,
                    "imageName" to resources.getResourceEntryName(it.imageResId)
                )
            },
            "isPublic" to false,
            "userId" to userId
        )

        Log.d("WorkoutSave", "Trying to save workout: $workoutMap")

        db.collection("users")
            .document(userId)
            .collection("workouts")
            .add(workoutMap)
            .addOnSuccessListener { documentReference ->
                val workoutId = documentReference.id
                Log.d("WorkoutSave", "Workout saved with ID: $workoutId")

                val savedWorkout = workout.copy(id = workoutId, userId = userId)

                val resultIntent = Intent()
                resultIntent.putExtra("workout", savedWorkout)
                setResult(RESULT_OK, resultIntent)

                Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("WorkoutSave", "Failed to save workout", e)
            }
    }
}

