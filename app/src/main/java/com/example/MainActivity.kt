package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ripple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SeriesEditorSheet
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GripForgeTheme
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.GripViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GripForgeTheme {
                GripForgeApp()
            }
        }
    }
}

enum class NavigationScreen(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    ANALYTICS("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GripForgeApp(
    viewModel: GripViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(NavigationScreen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe ViewModel states
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val seriesList by viewModel.seriesList.collectAsStateWithLifecycle()
    val activeDraft by viewModel.activeDraft.collectAsStateWithLifecycle()
    val grippers by viewModel.gripperLocker.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSecondsRemaining.collectAsStateWithLifecycle()
    val restTotal by viewModel.restTotalSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val selectedHorizon by viewModel.selectedHorizon.collectAsStateWithLifecycle()
    val analyticsSummary by viewModel.analyticsSummary.collectAsStateWithLifecycle()

    // Compute today's and lifetime stats for Profile screen
    val todayKey = remember(seriesList) { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayTotalReps = remember(seriesList) {
        seriesList.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.series.createdAt)) == todayKey
        }.sumOf { it.totalReps }
    }
    val lifetimeTotalReps = analyticsSummary?.totalRepsAllTime ?: 0
    val lifetimeLeftReps = analyticsSummary?.leftRepsAllTime ?: 0
    val lifetimeRightReps = analyticsSummary?.rightRepsAllTime ?: 0

    // Handle back button behavior
    BackHandler(enabled = currentScreen != NavigationScreen.HOME || activeDraft != null) {
        if (activeDraft != null) {
            viewModel.discardDraft()
        } else if (currentScreen != NavigationScreen.HOME) {
            currentScreen = NavigationScreen.HOME
        }
    }

    // Listen to rest timer completion events for alert feedback
    LaunchedEffect(Unit) {
        viewModel.timerFinishedEvent.collectLatest {
            scope.launch {
                snackbarHostState.showSnackbar("⏱ Rest interval completed! Time to squeeze again.")
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            // Main Content Area with screen transitions
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    NavigationScreen.HOME -> {
                        HomeScreen(
                            userProfile = userProfile,
                            seriesList = seriesList,
                            grippers = grippers,
                            restSecondsRemaining = restSeconds,
                            restTotalSeconds = restTotal,
                            isTimerRunning = isTimerRunning,
                            onStartRestPreset = { seconds -> viewModel.startRestTimer(seconds) },
                            onPauseTimer = { viewModel.pauseTimer() },
                            onResumeTimer = { viewModel.resumeTimer() },
                            onResetTimer = { viewModel.resetTimer() },
                            onAddTimerSeconds = { delta -> viewModel.addTimerSeconds(delta) },
                            onQuickStartSession = { gripperName -> viewModel.quickStartSession(gripperName) },
                            onStartNewSeries = { templateName -> viewModel.startNewSeries(templateName) },
                            onLogQuickSqueeze = { hand -> viewModel.logQuickSqueeze(hand, 1) },
                            onEditSeries = { seriesWithSets -> viewModel.editExistingSeries(seriesWithSets) },
                            onDeleteSeries = { seriesId -> viewModel.deleteSeries(seriesId) },
                            onUpdateAthleteName = { newName -> viewModel.updateAthleteName(newName) }
                        )
                    }
                    NavigationScreen.ANALYTICS -> {
                        AnalyticsScreen(
                            summary = analyticsSummary,
                            selectedHorizon = selectedHorizon,
                            onSelectHorizon = { horizon -> viewModel.selectHorizon(horizon) }
                        )
                    }
                    NavigationScreen.PROFILE -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            grippers = grippers,
                            todayTotalReps = todayTotalReps,
                            lifetimeTotalReps = lifetimeTotalReps,
                            lifetimeLeftReps = lifetimeLeftReps,
                            lifetimeRightReps = lifetimeRightReps,
                            onUpdateProfile = { name, unit, goalReps, rankTitle, defaultRest ->
                                viewModel.updateProfileSettings(name, unit, goalReps, rankTitle, defaultRest)
                            },
                            onAddGripper = { name, rating, type ->
                                viewModel.addGripperEquipment(name, rating, type)
                            },
                            onRemoveGripper = { id ->
                                viewModel.removeGripperEquipment(id)
                            }
                        )
                    }
                }
            }

            // FLOATING CURVED NAVIGATION BAR
            FloatingCurvedNavBar(
                currentScreen = currentScreen,
                onSelectScreen = { screen -> currentScreen = screen },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }

    // Active Series Editor Modal Sheet
    activeDraft?.let { draft ->
        SeriesEditorSheet(
            draft = draft,
            onUpdateName = { name -> viewModel.updateDraftName(name) },
            onUpdateNotes = { notes -> viewModel.updateDraftNotes(notes) },
            onAddSet = { hand -> viewModel.addSetToDraft(hand) },
            onRemoveSet = { tempId -> viewModel.removeSetFromDraft(tempId) },
            onIncrementReps = { tempId, delta -> viewModel.incrementReps(tempId, delta) },
            onSetRepsDirect = { tempId, newReps -> viewModel.setRepsDirect(tempId, newReps) },
            onSetWeightDirect = { tempId, weight -> viewModel.setWeightDirect(tempId, weight) },
            onToggleHand = { tempId -> viewModel.toggleHand(tempId) },
            onMoveUp = { tempId -> viewModel.moveSetUp(tempId) },
            onMoveDown = { tempId -> viewModel.moveSetDown(tempId) },
            onSave = { viewModel.saveDraft() },
            onDismiss = { viewModel.discardDraft() }
        )
    }
}

/**
 * Modern Floating Curved Navigation Bar
 * Features elevated rounded pill shape, high-contrast active icons, and safe insets.
 */
@Composable
fun FloatingCurvedNavBar(
    currentScreen: NavigationScreen,
    onSelectScreen: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF141824).copy(alpha = 0.96f),
        tonalElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = FlameOrange.copy(alpha = 0.25f),
                ambientColor = Color.Black
            )
            .border(1.dp, Color(0xFF263044), RoundedCornerShape(32.dp))
            .testTag("floating_navigation_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen

                val activeColor by animateColorAsState(
                    targetValue = when (screen) {
                        NavigationScreen.HOME -> FlameOrange
                        NavigationScreen.ANALYTICS -> ElectricCyan
                        NavigationScreen.PROFILE -> Color(0xFFFFB74D)
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tabColor"
                )

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent,
                    label = "tabBgColor"
                )

                // Minimum touch target of 48dp satisfied with padding
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = activeColor)
                        ) { onSelectScreen(screen) }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .testTag("nav_tab_${screen.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.label,
                            tint = if (isSelected) activeColor else SlateMuted,
                            modifier = Modifier.size(22.dp)
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = activeColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
