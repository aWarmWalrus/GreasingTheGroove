package com.charlesq.greasingthegroove

fun getPredefinedExercises(): List<Exercise> {
    return listOf(
        // Repetition-based exercises
        Exercise(
            id = "pull_ups",
            name = "Pull-ups",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "push_ups",
            name = "Push-ups",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CHEST,
            otherTargets = listOf(BodyPart.ARMS, BodyPart.CORE),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "squats",
            name = "Squats",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.SQUAT
        ),
        Exercise(
            id = "dips",
            name = "Dips",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CHEST),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "lunges",
            name = "Lunges",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            movementPattern = MovementPattern.LUNGE
        ),
        Exercise(
            id = "calf_raises",
            name = "Calf Raises",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            movementPattern = MovementPattern.SQUAT
        ),
        Exercise(
            id = "hanging_leg_raises",
            name = "Hanging Leg Raises",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "pistol_squat",
            name = "Pistol Squat",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.SQUAT
        ),
        Exercise(
            id = "bench_press",
            name = "Bench Press",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CHEST,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "deadlift",
            name = "Deadlift",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.LEGS, BodyPart.CORE),
            movementPattern = MovementPattern.HINGE
        ),
        Exercise(
            id = "hammer_curl",
            name = "Hammer Curl",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "incline_curl",
            name = "Incline Curl",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "chin_up",
            name = "Chin Up",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.BACK),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "bent_over_row",
            name = "Bent Over Row",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "scapular_pull_up",
            name = "Scapular Pull Up",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "overhead_press",
            name = "Overhead Press",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "arnold_press",
            name = "Arnold Press",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "lateral_raises",
            name = "Lateral Raises",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "pike_push_ups",
            name = "Pike Push-ups",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CHEST),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "face_pulls",
            name = "Face Pulls",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "glute_bridges",
            name = "Glute Bridges",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.HINGE
        ),
        Exercise(
            id = "hip_thrusts",
            name = "Hip Thrusts",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.HINGE
        ),
        Exercise(
            id = "romanian_deadlifts",
            name = "Romanian Deadlifts",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            otherTargets = listOf(BodyPart.BACK, BodyPart.CORE),
            movementPattern = MovementPattern.HINGE
        ),
        Exercise(
            id = "step_ups",
            name = "Step Ups",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.LEGS,
            movementPattern = MovementPattern.LUNGE
        ),
        Exercise(
            id = "incline_bench_press",
            name = "Incline Bench Press",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CHEST,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "dumbbell_flys",
            name = "Dumbbell Flys",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CHEST,
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "lat_pulldowns",
            name = "Lat Pulldowns",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "seated_cable_rows",
            name = "Seated Cable Rows",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "back_extensions",
            name = "Back Extensions",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.LEGS),
            movementPattern = MovementPattern.HINGE
        ),
        Exercise(
            id = "triceps_pushdowns",
            name = "Triceps Pushdowns",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "skull_crushers",
            name = "Skull Crushers",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "preacher_curls",
            name = "Preacher Curls",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.ARMS,
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "russian_twists",
            name = "Russian Twists",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.CORE_AND_CARRY
        ),
        Exercise(
            id = "leg_raises",
            name = "Leg Raises",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.SQUAT
        ),
        Exercise(
            id = "ab_rollouts",
            name = "Ab Rollouts",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "mountain_climbers",
            name = "Mountain Climbers",
            metric = MetricType.REPS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.CORE_AND_CARRY
        ),


        // Isometric / Duration-based exercises
        Exercise(
            id = "plank",
            name = "Plank",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.CORE_AND_CARRY
        ),
        Exercise(
            id = "wall_sit",
            name = "Wall Sit",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.LEGS,
            movementPattern = MovementPattern.SQUAT
        ),
        Exercise(
            id = "hollow_body_hold",
            name = "Hollow Body Hold",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.CORE_AND_CARRY
        ),
        Exercise(
            id = "dead_hang",
            name = "Dead Hang",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.BACK),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "l_sit",
            name = "L-Sit",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CORE,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "handstand",
            name = "Hand Stand",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "side_plank",
            name = "Side Plank",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CORE,
            movementPattern = MovementPattern.CORE_AND_CARRY
        ),
        Exercise(
            id = "hanging_l_sit",
            name = "Hanging L Sit",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CORE,
            otherTargets = listOf(BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "tuck_planche",
            name = "Tuck Planche",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CORE, BodyPart.CHEST),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "straddle_planche",
            name = "Straddle Planche",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CORE, BodyPart.CHEST),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "front_lever",
            name = "Front Lever",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.BACK,
            otherTargets = listOf(BodyPart.CORE, BodyPart.ARMS),
            movementPattern = MovementPattern.PULL
        ),
        Exercise(
            id = "back_lever",
            name = "Back Lever",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CHEST,
            otherTargets = listOf(BodyPart.CORE, BodyPart.ARMS),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "human_flag",
            name = "Human Flag",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.CORE,
            otherTargets = listOf(BodyPart.ARMS, BodyPart.BACK),
            movementPattern = MovementPattern.CORE_AND_CARRY
        ),
        Exercise(
            id = "frog_stand",
            name = "Frog Stand",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.PUSH
        ),
        Exercise(
            id = "crow_pose",
            name = "Crow Pose",
            metric = MetricType.ISOMETRICS,
            primaryTarget = BodyPart.ARMS,
            otherTargets = listOf(BodyPart.CORE),
            movementPattern = MovementPattern.PUSH
        )
    )
}
