## FitPlan – Personal Workout Planner 💪
*FitPlan* is a comprehensive Android fitness application designed to help users create, manage, and share personalized workout plans.

## Features
# My Workout Plan 🏋️
- Create custom workouts by selecting from a built-in local exercise library (hardcoded in the app).
- Exercises are categorized and include a name, image, and detailed description.
- Workouts can be deleted anytime.
- View saved workouts in a structured list using RecyclerView.
- Tap on a workout to view its exercises; tap on an exercise to open a dialog with image and instructions.
- Add your own custom exercises (name + description), which will be visible only to you.
- Set and update working weights for each exercise.
- Add a workout reminder to your personal device calendar with date and time.
- Save user-created workouts to *Firebase Firestore*, linked to your account.

# Profile 👤
- Update profile information: photo, username, bio, and trainer/trainee status.
- Information is stored and retrieved from Firebase Firestore, and photo from Firebase Storage.

# Social Media 🌐
- Share workouts publicly via a toggle switch ("Make Public").
- Public workouts can be made private again using the toggle switch ("Make Private"). It will delete them from Social Media.
- Public workouts appear in a dedicated Social Media screen with other users' profiles.
- Tap a workout to preview its details in a dialog.
- Tap a username to preview the profile of the user who created the workout.
- "Save Workout" adds the public workout to your personal saved list.

# Saved Workouts 📥
- List of workouts you saved from Social Media.
- Saved Workouts remain available in your list, even if their original creators later make them private.
- Workouts can be deleted from this list.

## Technologies Used
- **Language:** Kotlin
- **External Services:**
    - Firebase Auth (user authentication, log-in & sign-up)
    - Firebase Firestore (cloud database)
    - Firebase Storage (profile images)
- **UI Components:** RecyclerView, Dialogs, ImageViews

## Credit
This project was developed by Ella Ben Shitrit as part of a final project for the Mobile Applications Development course.

## How to Run the App
Clone the repository:
```bash
    git clone https://github.com/EllaBenShitrit/FitPlan.git



