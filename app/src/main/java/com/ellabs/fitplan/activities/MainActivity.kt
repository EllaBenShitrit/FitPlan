package com.ellabs.fitplan.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ellabs.fitplan.R
import com.ellabs.fitplan.managers.ProfileManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var tvUserName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvBio: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var btnEditProfile: Button
    private lateinit var btnMyWorkoutPlan: Button
    private lateinit var btnSocialMedia: Button
    private lateinit var btnSavedWorkouts: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvUserName = findViewById(R.id.tvUserName)
        tvStatus = findViewById(R.id.tvStatus)
        tvBio = findViewById(R.id.tvBio)
        imageProfile = findViewById(R.id.imageProfile)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnMyWorkoutPlan = findViewById(R.id.btnMyWorkoutPlan)
        btnSocialMedia = findViewById(R.id.btnSocialMedia)
        btnSavedWorkouts = findViewById(R.id.btnSavedWorkouts)

        // Open "Edit Profile" screen
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Open "My Workout Plan" screen
        btnMyWorkoutPlan.setOnClickListener {
            val intent = Intent(this, MyWorkoutPlanActivity::class.java)
            startActivity(intent)
        }

        // Open "Social Media" screen
        btnSocialMedia.setOnClickListener {
            val intent = Intent(this, SocialMediaActivity::class.java)
            startActivity(intent)
        }

        // Open "Saved Workouts" screen
        btnSavedWorkouts.setOnClickListener {
            val intent = Intent(this, SocialMediaSavedWorkoutsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileFromFirestore() // Load profile from Firestore
    }

    private fun loadProfileFromFirestore() {
        val currentUser = Firebase.auth.currentUser ?: return

        ProfileManager.loadProfile(
            uid = currentUser.uid,
            onSuccess = { profile ->
                // Show default values if empty
                tvUserName.text = if (profile.username.isNullOrBlank()) "User Name" else profile.username
                tvStatus.text = if (profile.status.isNullOrBlank() || profile.status == "Choose status") "Status" else profile.status
                tvBio.text = if (profile.bio.isNullOrBlank()) "Bio" else profile.bio

                // Show profile image if exists, else show default
                if (!profile.imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(profile.imageUrl).into(imageProfile)
                } else {
                    imageProfile.setImageResource(R.drawable.profile)
                }
            },
            onFailure = { error ->
                Log.e("MainActivity", "Failed to load profile", error)

                // In case of error – show default UI
                tvUserName.text = "User Name"
                tvStatus.text = "Status"
                tvBio.text = "Bio"
                imageProfile.setImageResource(R.drawable.profile)
            }
        )
    }

//    override fun onResume() {
//        super.onResume()
//
//        // Load saved data of user's profile
//        val prefs: SharedPreferences = getSharedPreferences("profile", MODE_PRIVATE)
//
//        val username = prefs.getString("username", "")
//        val status = prefs.getString("status", "")
//        val bio = prefs.getString("bio", "")
//
//
//        tvUserName.text = if (username.isNullOrEmpty()) "User Name" else username
//        tvStatus.text = if (status.isNullOrEmpty() || status == "Choose status") "Status" else status
//        tvBio.text = if (bio.isNullOrEmpty()) "Bio" else bio
//
//        val file = File(filesDir, "profile.jpg")
//        if (file.exists()) {
//            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
//            imageProfile.setImageBitmap(bitmap)
//        } else {
//            imageProfile.setImageResource(R.drawable.profile) // תמונת ברירת מחדל
//        }
//
//    }

}