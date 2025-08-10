package com.ellabs.fitplan.activities

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ellabs.fitplan.R
import com.ellabs.fitplan.databinding.ActivityEditProfileBinding
import com.ellabs.fitplan.managers.ProfileManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var storageRef: StorageReference
    private lateinit var binding: ActivityEditProfileBinding
    private var fileUri: Uri? = null
    private var currentImageUrl: String? = null

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            fileUri = uri  // Upload image URL that user chose
            binding.imageEditProfile.setImageURI(uri) // Show the photo that user chose

        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = findViewById(R.id.imageEditProfile)
        storageRef = Firebase.storage.reference

        initViews()
        loadProfileFromFirestore() // Load user's profile data into the edit fields
    }


    private fun initViews() {
        binding.btnEditPicture.setOnClickListener {
            openMediaPicker()
        }

        binding.btnSaveProfile.setOnClickListener {
            uploadPhotoAndSaveProfile()
        }

        setUpStatusSpinner(null)
    }


    private fun openMediaPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadPhotoAndSaveProfile() {
        val currentUser = Firebase.auth.currentUser ?: return
        val uri = fileUri

        if (uri == null) {
            Log.d("EditProfile", "No image selected, saving text only")
            saveProfileToFirestore(currentImageUrl)
            return
        }

        val imageRef = storageRef.child("images/${currentUser.uid}") // Define path in Storage under 'images/{uid}'
        val uploadTask = imageRef.putFile(uri) // Upload the file to Firebase Storage

        // Get the public download URL and pass it to Firestore
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("EditProfile", "Image uploaded successfully: $downloadUri")
                saveProfileToFirestore(downloadUri.toString()) // Save full profile data (including image URL) to Firestore
            } else {
                Log.e("EditProfile", "Upload failed", task.exception)
            }
        }
    }

    private fun saveProfileToFirestore(imageUrl: String?) {
        val currentUser = Firebase.auth.currentUser ?: return

        val userMap = mapOf(
            "uid" to currentUser.uid,
            "username" to binding.editUsername.text.toString(),
            "bio" to binding.editBio.text.toString(),
            "status" to binding.spinnerStatus.selectedItem.toString(),
            "imageUrl" to imageUrl
        )

        Firebase.firestore.collection("users").document(currentUser.uid)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("EditProfile", "Profile saved successfully")
                finish() // Return to Main Activity screen
            }
            .addOnFailureListener {
                Log.e("EditProfile", "Failed to save profile", it)
            }
    }

    private fun loadProfileFromFirestore() {
        val currentUser = Firebase.auth.currentUser ?: return

        ProfileManager.loadProfile(
            uid = currentUser.uid,
            onSuccess = { profile ->
                binding.editUsername.setText(profile.username)
                binding.editBio.setText(profile.bio)
                setUpStatusSpinner(profile.status)

                currentImageUrl = profile.imageUrl
                if (profile.imageUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(profile.imageUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding.imageEditProfile)
                }
            },
            onFailure = { error ->
                Log.e("EditProfile", "Failed to load profile", error)
            }
        )
    }


    //  Set up spinner values for status selection, with optional pre-selected value
    private fun setUpStatusSpinner(savedStatus: String?) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.status_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        val statusOptions = resources.getStringArray(R.array.status_options)
        val index = statusOptions.indexOf(savedStatus)
        binding.spinnerStatus.setSelection(if (index >= 0) index else 0)
    }
}


