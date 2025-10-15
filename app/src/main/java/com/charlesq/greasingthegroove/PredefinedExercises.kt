package com.charlesq.greasingthegroove

fun getPredefinedExercises(): List<Exercise> {
    return listOf(
        // Repetition-based exercises
        Exercise(id = "pull_ups", name = "Pull-ups", exerciseMode = "REPS"),
        Exercise(id = "push_ups", name = "Push-ups", exerciseMode = "REPS"),
        Exercise(id = "squats", name = "Squats", exerciseMode = "REPS"),
        Exercise(id = "dips", name = "Dips", exerciseMode = "REPS"),
        Exercise(id = "lunges", name = "Lunges", exerciseMode = "REPS"),
        Exercise(id = "calf_raises", name = "Calf Raises", exerciseMode = "REPS"),
        Exercise(id = "hanging_leg_raises", name = "Hanging Leg Raises", exerciseMode = "REPS"),

        // Isometric / Duration-based exercises
        Exercise(id = "plank", name = "Plank", exerciseMode = "ISOMETRICS"),
        Exercise(id = "wall_sit", name = "Wall Sit", exerciseMode = "ISOMETRICS"),
        Exercise(id = "hollow_body_hold", name = "Hollow Body Hold", exerciseMode = "ISOMETRICS"),
        Exercise(id = "dead_hang", name = "Dead Hang", exerciseMode = "ISOMETRICS"),
        Exercise(id = "l_sit", name = "L-Sit", exerciseMode = "ISOMETRICS")
    )
}
