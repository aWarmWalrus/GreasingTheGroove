package com.charlesq.greasingthegroove

fun getPredefinedExercises(): List<Exercise> {
    return listOf(
        // Repetition-based exercises
        Exercise(id = "pull_ups", name = "Pull-ups", metric = MetricType.REPS),
        Exercise(id = "push_ups", name = "Push-ups", metric = MetricType.REPS),
        Exercise(id = "squats", name = "Squats", metric = MetricType.REPS),
        Exercise(id = "dips", name = "Dips", metric = MetricType.REPS),
        Exercise(id = "lunges", name = "Lunges", metric = MetricType.REPS),
        Exercise(id = "calf_raises", name = "Calf Raises", metric = MetricType.REPS),
        Exercise(id = "hanging_leg_raises", name = "Hanging Leg Raises", metric = MetricType.REPS),
        Exercise(id = "pistol_squat", name = "Pistol Squat", metric = MetricType.REPS),
        Exercise(id = "bench_press", name = "Bench Press", metric = MetricType.REPS),
        Exercise(id = "deadlift", name = "Deadlift", metric = MetricType.REPS),
        Exercise(id = "hammer_curl", name = "Hammer Curl", metric = MetricType.REPS),
        Exercise(id = "incline_curl", name = "Incline Curl", metric = MetricType.REPS),
        Exercise(id = "chin_up", name = "Chin Up", metric = MetricType.REPS),
        Exercise(id = "bent_over_row", name = "Bent Over Row", metric = MetricType.REPS),
        Exercise(id = "scapular_pull_up", name = "Scapular Pull Up", metric = MetricType.REPS),


        // Isometric / Duration-based exercises
        Exercise(id = "plank", name = "Plank", metric = MetricType.ISOMETRICS),
        Exercise(id = "wall_sit", name = "Wall Sit", metric = MetricType.ISOMETRICS),
        Exercise(id = "hollow_body_hold", name = "Hollow Body Hold", metric = MetricType.ISOMETRICS),
        Exercise(id = "dead_hang", name = "Dead Hang", metric = MetricType.ISOMETRICS),
        Exercise(id = "l_sit", name = "L-Sit", metric = MetricType.ISOMETRICS),
        Exercise(id = "handstand", name = "Hand Stand", metric = MetricType.ISOMETRICS),
        Exercise(id = "side_plank", name = "Side Plank", metric = MetricType.ISOMETRICS),
        Exercise(id = "hanging_l_sit", name = "Hanging L Sit", metric = MetricType.ISOMETRICS)
    )
}
