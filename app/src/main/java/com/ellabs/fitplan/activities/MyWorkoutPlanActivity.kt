package com.ellabs.fitplan.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.adapters.WorkoutsAdapter
import com.ellabs.fitplan.classes.Exercise
import com.ellabs.fitplan.classes.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyWorkoutPlanActivity : AppCompatActivity() {
    private lateinit var tvEmptyMessage: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddWorkout: Button
    private lateinit var btnBack: Button

    private lateinit var workoutsAdapter: WorkoutsAdapter
    private val workoutsList = mutableListOf<Workout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_workout_plan)

        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        recyclerView = findViewById(R.id.recyclerViewWorkouts)
        btnAddWorkout = findViewById(R.id.btnAddWorkout)
        btnBack = findViewById(R.id.btnBack)

        workoutsAdapter = WorkoutsAdapter(workoutsList, object : WorkoutsAdapter.OnWorkoutClickListener {
            override fun onWorkoutClicked(workout: Workout) {
                // Build an Intent for WorkoutDetailsActivity screen of the selected workout
                val intent = Intent(this@MyWorkoutPlanActivity, WorkoutDetailsActivity::class.java)
                intent.putExtra("workout", workout)
                startActivity(intent) // Start WorkoutDetailsActivity
            }
        })


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = workoutsAdapter

        btnAddWorkout.setOnClickListener {
            startActivity(Intent(this, CreateWorkoutActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish() // Return to MainActivity
        }
    }

    override fun onResume() {
        super.onResume()
        loadWorkoutsFromFirestore()
    }

    private fun loadWorkoutsFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("workouts")
            .get()
            .addOnSuccessListener { result ->
                workoutsList.clear()
                for (doc in result) {
                    val title = doc.getString("title") ?: continue
                    val exercisesData = doc.get("exercises") as? List<HashMap<String, String>> ?: continue

                    val exercises = exercisesData.map {
                        Exercise(
                            it["name"] ?: "",
                            resources.getIdentifier(it["imageName"], "drawable", packageName),
                            it["description"] ?: ""
                        )
                    }

                    val workoutId = doc.id
                    val userIdFromDoc = doc.getString("userId") ?: ""
                    workoutsList.add(
                        Workout(
                            id = workoutId,
                            title = title,
                            exercises = exercises,
                            userId = userIdFromDoc

                        )
                    )
                }

                if (workoutsList.isEmpty()) {
                    tvEmptyMessage.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmptyMessage.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    workoutsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Log.e("MyWorkoutPlanActivity", "Failed to load workouts", it)
            }
    }
}