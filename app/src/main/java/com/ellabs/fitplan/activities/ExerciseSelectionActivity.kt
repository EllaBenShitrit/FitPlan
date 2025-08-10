package com.ellabs.fitplan.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.classes.Exercise
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ellabs.fitplan.managers.Constants
import com.ellabs.fitplan.adapters.ExerciseSelectionAdapter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson


class ExerciseSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var adapter: ExerciseSelectionAdapter
    private lateinit var btnBack: Button
    private lateinit var btnAddNewExercise: Button

    private val allExercises = mutableListOf<Exercise>() // All exercises of the chosen category
    private val selectedExercises = mutableListOf<Exercise>() // All exercises that the user actually selected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_selection)

        recyclerView = findViewById(R.id.recyclerViewExercises)
        btnSave = findViewById(R.id.btnSave)
        adapter = ExerciseSelectionAdapter(allExercises, selectedExercises)
        btnBack = findViewById(R.id.btnBack)
        btnAddNewExercise = findViewById(R.id.btnAddNewExercise)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Get the chosen category from the Intent of CreateWorkoutActivity
        val category = intent.getStringExtra("category") ?: return

        val builtInExercises = getExercisesForCategory(category)
        val localExercises = getLocalExercises(category)
        allExercises.clear()
        allExercises.addAll(builtInExercises + localExercises)
        adapter.notifyDataSetChanged()

        // By saving the chosen exercises, it will be returned to CreateWorkoutActivity
        btnSave.setOnClickListener {
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra(
                    Constants.EXTRA_SELECTED_EXERCISES,
                    ArrayList(selectedExercises)
                )
            }
            setResult(RESULT_OK, resultIntent)
            finish()  // Return to CreateWorkoutActivity screen after saving the chosen exercises
        }

        btnAddNewExercise.setOnClickListener {
            showAddExerciseDialog()
        }

        btnBack.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Discard exercises?")
                .setMessage("You have unsaved changes. Are you sure you want to go back?")
                .setPositiveButton("Yes") { _, _ -> finish() } // Return to CreateWorkoutActivity screen without saving anything
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Return exercises according to the category
    private fun getExercisesForCategory(category: String): List<Exercise> {
        return when (category) {
            "lower_body" -> listOf(
                Exercise("Squats", R.drawable.squat, "Stand shoulder-width apart, lower hips like sitting in a chair, keep chest up, and rise back. Full lower-body strength."),
                Exercise("Lunges", R.drawable.lunges, "Step forward, lower both knees to 90°, push back to standing. Strengthens quads, hamstrings, glutes."),
                Exercise("Leg Extension", R.drawable.legextension, "Sit at machine, extend legs by straightening knees, then lower. Isolates quads."),
                Exercise("Step Ups", R.drawable.stepup, "Step onto a bench with one leg, push through heel to lift body, then step down. Focus on quads and glutes."),
                Exercise("Deadlift", R.drawable.deadlift, "Stand with feet hip-width, bend at hips and knees, lift weight while keeping back straight. Strengthens hamstrings and lower back."),
                Exercise("Leg Curl", R.drawable.legcurl, "Lie or sit on a machine, curl heels toward butt, then release slowly. Isolates hamstrings."),
                Exercise("Hip Thrust", R.drawable.hipthrust, "Sit against bench, barbell on hips, thrust hips up until body is straight, then lower. Strong glute builder.")
            )

            "upper_body" -> listOf(
                Exercise("Bench Press", R.drawable.benchpress, "Lie on a bench, grip the bar wider than shoulders, lower it to your chest, and push back up. Builds chest, shoulders, and triceps."),
                Exercise("Push Ups", R.drawable.pushups, "Keep your body straight, lower your chest to the floor by bending your elbows, then push back up. Strengthens chest and arms."),
                Exercise("Chest Fly", R.drawable.chestfly, "Lie on a bench with dumbbells, arms slightly bent, open arms wide then bring them together. Focuses on chest muscles."),
                Exercise("Pull Ups", R.drawable.pullups, "Grip the bar with palms facing away, pull your chin above the bar, and lower back down. Works lats and upper back."),
                Exercise("Lat Pulldown", R.drawable.latpulldown, "Sit at the machine, grip the bar wide, pull it to your upper chest, then release slowly. Targets the latissimus dorsi."),
                Exercise("Bent Over Row", R.drawable.bentoverrow, "Bend at hips with straight back, pull weights to your waist, squeeze shoulder blades. Strengthens mid-back."),
                Exercise("Shoulder Press", R.drawable.shoulderpress, "Hold weights at shoulder height, press them upward until arms are straight, then lower. Builds overall shoulder strength."),
                Exercise("Lateral Raise", R.drawable.lateralraise, "Raise dumbbells to the sides until arms are parallel to the floor, then lower. Targets lateral delts."),
                Exercise("Front Raise", R.drawable.frontraise, "Lift dumbbells straight in front of you to shoulder height, then lower. Works front delts."),
                Exercise("Bicep Curl", R.drawable.bicepcurl, "Hold dumbbells with palms up, curl toward shoulders while keeping elbows close. Builds biceps."),
                Exercise("Tricep Dips", R.drawable.tricepdips, "Use a bench or parallel bars, lower your body by bending elbows, then push back up. Trains triceps."),
            )

            "cardio" -> listOf(
                Exercise("Jump Rope", R.drawable.jumprope, "Hold rope handles, swing over head and jump as it passes under feet. Great cardio and coordination."),
                Exercise("Mountain Climbers", R.drawable.mountainclimbers, "In plank position, alternate knees toward chest quickly. Cardio + core."),
                Exercise("Jumping Jacks", R.drawable.jumpingjacks, "Jump while spreading arms and legs, return to start. Classic warm-up move."),
                Exercise("Plank", R.drawable.plank, "Hold your body in a straight line from head to heels, supported on elbows and toes. Strengthens core stability."),
                Exercise("Russian Twists", R.drawable.russiantwists, "Sit with knees bent, lean back slightly, twist torso side to side holding a weight. Engages obliques and core."),
                Exercise("Bicycle Crunches", R.drawable.bicyclecrunches, "Lie on your back, alternate bringing opposite elbow to knee in pedaling motion. Activates abs and obliques.")
            )

            else -> emptyList()
        }
    }

    private fun showAddExerciseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etExerciseTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExerciseDescription)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add New Exercise")
            .setPositiveButton("Save") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    val newExercise = Exercise(
                        title,
                        R.drawable.newexercisephoto,
                        description
                    )

                    // Save to SharedPreferences
                    val category = intent.getStringExtra("category") ?: ""
                    addExerciseToLocalDatabase(category, newExercise)

                    // Add to exercises list that shown on screen
                    allExercises.add(newExercise)
                    adapter.notifyItemInserted(allExercises.size - 1)

                    Log.d("NewExerciseSave", "Exercise Saved")
                    dialog.dismiss()

                } else {
                    Toast.makeText(this, "Please enter exercise name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()

    }


    fun addExerciseToLocalDatabase(category: String, exercise: Exercise) {
        val prefs = getSharedPreferences("my_exercises", Context.MODE_PRIVATE)
        val key = "category_$category"
        val list = getLocalExercises(category).toMutableList()
        list.add(exercise)

        val json = Gson().toJson(list)
        prefs.edit().putString(key, json).apply()
    }

    fun getLocalExercises(category: String): List<Exercise> {
        val prefs = getSharedPreferences("my_exercises", Context.MODE_PRIVATE)
        val key = "category_$category"
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Exercise>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }


    // Function for Debug (delete exercise that I added in "Add new exercise" option)
    fun removeExerciseFromLocalDatabase(category: String, exerciseName: String) {
        val prefs = getSharedPreferences("my_exercises", Context.MODE_PRIVATE)
        val key = "category_$category"
        val json = prefs.getString(key, null)

        if (json != null) {
            val type = object : TypeToken<List<Exercise>>() {}.type
            val exercises = Gson().fromJson<List<Exercise>>(json, type).toMutableList()

            val iterator = exercises.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().name.equals(exerciseName, ignoreCase = true)) {
                    iterator.remove()
                }
            }

            val updatedJson = Gson().toJson(exercises)
            prefs.edit().putString(key, updatedJson).apply()

            Log.d("DEBUG_FLOW", "Exercise '$exerciseName' removed from category $category.")
        } else {
            Log.d("DEBUG_FLOW", "No exercises found for category $category.")
        }
    }


}