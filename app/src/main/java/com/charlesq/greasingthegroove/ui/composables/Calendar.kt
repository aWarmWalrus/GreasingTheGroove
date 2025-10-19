package com.charlesq.greasingthegroove.ui.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.charlesq.greasingthegroove.CompletedSet
import com.charlesq.greasingthegroove.DashboardViewModel
import com.charlesq.greasingthegroove.Exercise
import com.charlesq.greasingthegroove.MovementPattern
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConsistencyCalendar(
    viewModel: DashboardViewModel,
    colorMap: Map<MovementPattern, Color>,
    predefinedExercises: List<Exercise>
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth.yearMonth }
            .filter { it != currentMonth }
            .collect { month ->
                uiState.currentUser?.let { user ->
                    Log.d("ConsistencyCalendar", "Fetching completed sets for ${month.month} ${month.year}")
                    viewModel.fetchCompletedSetsForMonth(month, user.uid)
                }
            }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val month = state.firstVisibleMonth.yearMonth
            val monthTitle =
                month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium
            )
            Row {
                IconButton(onClick = {
                    Log.d("ConsistencyCalendar", "Previous month button clicked")
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                IconButton(onClick = {
                    Log.d("ConsistencyCalendar", "Next month button clicked")
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day,
                    isToday = day.date == LocalDate.now(),
                    isPast = day.date.isBefore(LocalDate.now()),
                    sets = uiState.completedSetsByDate[day.date].orEmpty(),
                    colorMap = colorMap,
                    predefinedExercises = predefinedExercises,
                    onClick = {
                        Log.d("ConsistencyCalendar", "Day clicked: ${day.date}")
                        viewModel.onDayClicked(day.date)
                    }
                )
            },
        )
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isToday: Boolean,
    isPast: Boolean,
    sets: List<CompletedSet>,
    colorMap: Map<MovementPattern, Color>,
    predefinedExercises: List<Exercise>,
    onClick: () -> Unit
) {
    val hasSets = sets.isNotEmpty()
    val dayHighlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val dayBackgroundColor = if (hasSets) dayHighlightColor else Color.Transparent
    val dayTextColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Makes it a square
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then( // Apply border if it's today
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(2.dp), // Padding inside the border
        contentAlignment = Alignment.Center
    ) {
        // This Box is for the background, which is now smaller due to the padding on the parent
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(dayBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            val movementPatternsDoneToday = sets.map { it.exerciseId }.distinct()
                .mapNotNull { exerciseId -> predefinedExercises.find { it.id == exerciseId }?.movementPattern }
                .toSet()

            val movementPatternOrder = listOf(
                MovementPattern.PUSH,
                MovementPattern.PULL,
                MovementPattern.SQUAT,
                MovementPattern.LUNGE,
                MovementPattern.HINGE,
                MovementPattern.CORE_AND_CARRY
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = dayTextColor.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )

                val radius = minOf(maxWidth, maxHeight) / 3

                if (isToday) {
                    movementPatternOrder.forEachIndexed { index, movementPattern ->
                        val angle = 2 * Math.PI / 6 * index
                        val x = radius * cos(angle).toFloat()
                        val y = radius * sin(angle).toFloat()
                        val isDone = movementPatternsDoneToday.contains(movementPattern)
                        val color = if (isDone) {
                            colorMap[movementPattern] ?: Color.Transparent
                        } else {
                            Color.Gray.copy(alpha = 0.2f)
                        }

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(x, y)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                } else if (isPast) {
                    movementPatternOrder.forEachIndexed { index, movementPattern ->
                        if (movementPatternsDoneToday.contains(movementPattern)) {
                            val angle = 2 * Math.PI / 6 * index
                            val x = radius * cos(angle).toFloat()
                            val y = radius * sin(angle).toFloat()
                            val color = colorMap[movementPattern] ?: Color.Transparent

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(x, y)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}
