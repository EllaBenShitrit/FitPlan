package com.ellabs.fitplan.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.adapters.SocialMediaWorkoutsAdapter
import com.ellabs.fitplan.classes.UserProfile
import com.ellabs.fitplan.classes.Workout
import androidx.recyclerview.widget.LinearLayoutManager
import com.ellabs.fitplan.classes.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class SocialMediaSavedWorkoutsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SocialMediaWorkoutsAdapter
    private lateinit var tvEmptyMessage: TextView
    private val savedWorkoutsList = mutableListOf<Pair<Workout, UserProfile>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_media_saved_workouts)

        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        btnBack = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewSavedWorkouts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SocialMediaWorkoutsAdapter(
            workouts = savedWorkoutsList,
            showDeleteButtonInsteadOfSave = true,
            onDeleteClick = { workout -> confirmDelete(workout) }        )

        recyclerView.adapter = adapter
        loadSavedWorkouts()

        btnBack.setOnClickListener {
            finish() // Return to MainActivity
        }

    }

    private fun loadSavedWorkouts() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("savedWorkouts")
            .get()
            .addOnSuccessListener { snapshot ->
                savedWorkoutsList.clear()

                for (doc in snapshot) {
                    val title = doc.getString("title") ?: ""
                    val exercisesData = doc.get("exercises") as? List<Map<String, Any>> ?: emptyList()

                    val exercises = exercisesData.map { exerciseMap ->
                        val imageName = exerciseMap["imageName"] as? String ?: ""
                        val imageResId = resources.getIdentifier(imageName, "drawable", packageName)

                        Exercise(
                            name = exerciseMap["name"] as? String ?: "",
                            description = exerciseMap["description"] as? String ?: "",
                            imageResId = imageResId
                        )
                    }


                    val workout = Workout(title, exercises).copy(id = doc.id)

                    val userProfile = UserProfile(
                        username = doc.getString("username") ?: "",
                        status = doc.getString("status") ?: "",
                        bio = doc.getString("bio") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )

                    savedWorkoutsList.add(workout to userProfile)
                }

                if (savedWorkoutsList.isEmpty()) {
                    tvEmptyMessage.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmptyMessage.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("SocialMediaSavedWorkoutsActivity", "Failed to load saved workouts", it)
            }
    }


    private fun deleteSavedWorkout(workout: Workout) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("savedWorkouts")
            .document(workout.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Workout deleted!", Toast.LENGTH_SHORT).show()
                savedWorkoutsList.removeIf { it.first.id == workout.id }
                adapter.notifyDataSetChanged()

                if (savedWorkoutsList.isEmpty()) {
                    tvEmptyMessage.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Log.e("SocialMediaSavedWorkoutsActivity", "Failed to delete workout", it)
            }
    }

    private fun confirmDelete(workout: Workout) {
        AlertDialog.Builder(this)
            .setTitle("Delete workout?")
            .setMessage("Are you sure you want to delete this saved workout?")
            .setPositiveButton("Yes") { _, _ ->
                deleteSavedWorkout(workout)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}