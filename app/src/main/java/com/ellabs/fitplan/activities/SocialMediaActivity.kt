package com.ellabs.fitplan.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.ellabs.fitplan.R
import com.ellabs.fitplan.classes.Workout
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ellabs.fitplan.adapters.SocialMediaWorkoutsAdapter
import com.ellabs.fitplan.classes.Exercise
import com.ellabs.fitplan.classes.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class SocialMediaActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SocialMediaWorkoutsAdapter
    private val workoutList = mutableListOf<Pair<Workout, UserProfile>>() // Pairs list of workout & user details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_media)
        recyclerView = findViewById(R.id.recyclerViewSocialFeed)
        btnBack = findViewById(R.id.btnBack)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SocialMediaWorkoutsAdapter(workoutList)
        recyclerView.adapter = adapter

        loadPublicWorkouts() // Retrieval all public workouts from all users

        btnBack.setOnClickListener {
            finish() // Return to MainActivity
        }
    }

    private fun loadPublicWorkouts() {
        workoutList.clear()
        val db = FirebaseFirestore.getInstance()

        db.collection("workouts")
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { workoutsSnapshot ->
                for (workoutDoc in workoutsSnapshot) {
                    val title = workoutDoc.getString("title") ?: continue
                    val exercisesData = workoutDoc.get("exercises") as? List<Map<String, String>> ?: continue
                    val workoutId = workoutDoc.id
                    val userId = workoutDoc.getString("userId") ?: continue

                    val exercises = exercisesData.map {
                        Exercise(
                            name = it["name"] ?: "",
                            imageResId = resources.getIdentifier(it["imageName"], "drawable", packageName),
                            description = it["description"] ?: ""
                        )
                    }

                    val workout = Workout(
                        id = workoutId,
                        title = title,
                        exercises = exercises,
                        isPublic = true,
                        userId = userId
                    )

                    // Loading user profile details
                    db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val username = userDoc.getString("username") ?: "Unknown"
                            val bio = userDoc.getString("bio") ?: ""
                            val status = userDoc.getString("status") ?: ""
                            val imageUrl = userDoc.getString("imageUrl") ?: ""

                            val userProfile = UserProfile(username, bio, status, imageUrl)
                            workoutList.add(workout to userProfile)
                            adapter.notifyDataSetChanged()
                            Log.d("UserProfileCheck", "Loaded user: ${userProfile.username}, profileImageUrl: ${userProfile.imageUrl}")

                        }
                }
            }
            .addOnFailureListener {
                Log.e("SocialMediaActivity", "Failed to load public workouts", it)
            }
    }
}