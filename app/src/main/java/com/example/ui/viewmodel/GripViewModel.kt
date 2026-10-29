package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.GripDatabase
import com.example.data.model.GripSeries
import com.example.data.model.GripSet
import com.example.data.model.GripperEquipment
import com.example.data.model.HandSide
import com.example.data.model.SeriesWithSets
import com.example.data.model.UserProfile
import com.example.data.repository.AnalyticsSummary
import com.example.data.repository.GripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DraftSet(
    val tempId: Long = System.nanoTime(),
    val originalId: Long = 0,
    val hand: HandSide = HandSide.RIGHT,
    val weightOrLevel: String = "40 kg",
    val reps: Int = 10
)

data class ActiveSeriesDraft(
    val id: Long = 0,
    val name: String = "Grip Series",
    val notes: String = "",
    val sets: List<DraftSet> = emptyList()
)

class GripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GripRepository
    private var timerJob: Job? = null

    // Vibrate event channel
    private val _timerFinishedEvent = MutableSharedFlow<Unit>()
    val timerFinishedEvent = _timerFinishedEvent.asSharedFlow()

    init {
        val db = GripDatabase.getInstance(application)
        repository = GripRepository(db.gripDao())

        viewModelScope.launch {
            repository.initializeStarterDataIfEmpty()
        }
    }

    // Series List
    val seriesList: StateFlow<List<SeriesWithSets>> = repository.allSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Profile
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile(name = "Alex") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile(name = "Alex"))

    // Active Workout Series Draft
    private val _activeDraft = MutableStateFlow<ActiveSeriesDraft?>(null)
    val activeDraft: StateFlow<ActiveSeriesDraft?> = _activeDraft.asStateFlow()

    // Gripper Equipment Locker (Persistent in-memory state with realistic defaults)
    private val _gripperLocker = MutableStateFlow(
        listOf(
            GripperEquipment("1", "Captains of Crush Trainer", "45 kg", "Torsion Spring"),
            GripperEquipment("2", "Captains of Crush #1", "63 kg", "Torsion Spring"),
            GripperEquipment("3", "Captains of Crush #1.5", "75 kg", "Torsion Spring"),
            GripperEquipment("4", "Adjustable Hand Spring", "10-40 kg", "Adjustable Coil"),
            GripperEquipment("5", "Heavy Grip 150", "68 kg", "Torsion Spring"),
            GripperEquipment("6", "Two-Hand Pinch Gripper", "35 kg", "Pinch Block")
        )
    )
    val gripperLocker: StateFlow<List<GripperEquipment>> = _gripperLocker.asStateFlow()

    // Rest Timer
    private val _restSecondsRemaining = MutableStateFlow(0)
    val restSecondsRemaining: StateFlow<Int> = _restSecondsRemaining.asStateFlow()

    private val _restTotalSeconds = MutableStateFlow(60)
    val restTotalSeconds: StateFlow<Int> = _restTotalSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // Analytics Time Horizon ("Week", "Month", "Year")
    private val _selectedHorizon = MutableStateFlow("Week")
    val selectedHorizon: StateFlow<String> = _selectedHorizon.asStateFlow()

    // Analytics Summary derived reactively
    val analyticsSummary: StateFlow<AnalyticsSummary?> = combine(
        seriesList,
        _selectedHorizon
    ) { series, horizon ->
        repository.computeAnalytics(series, horizon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Draft Management
    fun startNewSeries(defaultName: String = "Hand Gripper Squeeze Session") {
        val defaultGripper = _gripperLocker.value.firstOrNull()?.ratingOrWeight ?: "45 kg"
        val initialSets = listOf(
            DraftSet(hand = HandSide.RIGHT, weightOrLevel = defaultGripper, reps = 12),
            DraftSet(hand = HandSide.LEFT, weightOrLevel = defaultGripper, reps = 12),
            DraftSet(hand = HandSide.RIGHT, weightOrLevel = defaultGripper, reps = 10),
            DraftSet(hand = HandSide.LEFT, weightOrLevel = defaultGripper, reps = 10)
        )
        _activeDraft.value = ActiveSeriesDraft(
            id = 0,
            name = defaultName,
            notes = "Crush squeeze with controlled negative",
            sets = initialSets
        )
    }

    fun quickStartSession(gripperName: String? = null) {
        val selectedRating = gripperName ?: (_gripperLocker.value.firstOrNull()?.let { "${it.name} (${it.ratingOrWeight})" } ?: "45 kg")
        val initialSets = listOf(
            DraftSet(hand = HandSide.RIGHT, weightOrLevel = selectedRating, reps = 15),
            DraftSet(hand = HandSide.LEFT, weightOrLevel = selectedRating, reps = 15),
            DraftSet(hand = HandSide.RIGHT, weightOrLevel = selectedRating, reps = 12),
            DraftSet(hand = HandSide.LEFT, weightOrLevel = selectedRating, reps = 12)
        )
        _activeDraft.value = ActiveSeriesDraft(
            id = 0,
            name = "Quick Crush Session",
            notes = "Rapid bilateral hand grip session",
            sets = initialSets
        )
    }

    fun editExistingSeries(seriesWithSets: SeriesWithSets) {
        val draftSets = seriesWithSets.sortedSets.map { set ->
            DraftSet(
                tempId = System.nanoTime() + set.id,
                originalId = set.id,
                hand = set.hand,
                weightOrLevel = set.weightOrLevel,
                reps = set.reps
            )
        }
        _activeDraft.value = ActiveSeriesDraft(
            id = seriesWithSets.series.id,
            name = seriesWithSets.series.name,
            notes = seriesWithSets.series.notes,
            sets = draftSets
        )
    }

    fun updateDraftName(name: String) {
        _activeDraft.value = _activeDraft.value?.copy(name = name)
    }

    fun updateDraftNotes(notes: String) {
        _activeDraft.value = _activeDraft.value?.copy(notes = notes)
    }

    fun addSetToDraft(hand: HandSide, defaultWeight: String = "45 kg", defaultReps: Int = 10) {
        val current = _activeDraft.value ?: return
        val newSet = DraftSet(
            hand = hand,
            weightOrLevel = if (current.sets.isNotEmpty()) current.sets.last().weightOrLevel else defaultWeight,
            reps = if (current.sets.isNotEmpty()) current.sets.last().reps else defaultReps
        )
        _activeDraft.value = current.copy(sets = current.sets + newSet)
    }

    fun removeSetFromDraft(tempId: Long) {
        val current = _activeDraft.value ?: return
        _activeDraft.value = current.copy(sets = current.sets.filter { it.tempId != tempId })
    }

    fun incrementReps(tempId: Long, delta: Int) {
        val current = _activeDraft.value ?: return
        _activeDraft.value = current.copy(
            sets = current.sets.map {
                if (it.tempId == tempId) it.copy(reps = (it.reps + delta).coerceIn(1, 999))
                else it
            }
        )
    }

    fun setRepsDirect(tempId: Long, newReps: Int) {
        val current = _activeDraft.value ?: return
        _activeDraft.value = current.copy(
            sets = current.sets.map {
                if (it.tempId == tempId) it.copy(reps = newReps.coerceIn(1, 999))
                else it
            }
        )
    }

    fun setWeightDirect(tempId: Long, weight: String) {
        val current = _activeDraft.value ?: return
        _activeDraft.value = current.copy(
            sets = current.sets.map {
                if (it.tempId == tempId) it.copy(weightOrLevel = weight)
                else it
            }
        )
    }

    fun toggleHand(tempId: Long) {
        val current = _activeDraft.value ?: return
        _activeDraft.value = current.copy(
            sets = current.sets.map {
                if (it.tempId == tempId) {
                    val nextHand = if (it.hand == HandSide.LEFT) HandSide.RIGHT else HandSide.LEFT
                    it.copy(hand = nextHand)
                } else it
            }
        )
    }

    fun moveSetUp(tempId: Long) {
        val current = _activeDraft.value ?: return
        val list = current.sets.toMutableList()
        val index = list.indexOfFirst { it.tempId == tempId }
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _activeDraft.value = current.copy(sets = list)
        }
    }

    fun moveSetDown(tempId: Long) {
        val current = _activeDraft.value ?: return
        val list = current.sets.toMutableList()
        val index = list.indexOfFirst { it.tempId == tempId }
        if (index >= 0 && index < list.size - 1) {
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _activeDraft.value = current.copy(sets = list)
        }
    }

    fun reorderDraftSets(fromIndex: Int, toIndex: Int) {
        val current = _activeDraft.value ?: return
        if (fromIndex in current.sets.indices && toIndex in current.sets.indices && fromIndex != toIndex) {
            val list = current.sets.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _activeDraft.value = current.copy(sets = list)
        }
    }

    fun saveDraft() {
        val current = _activeDraft.value ?: return
        if (current.sets.isEmpty()) return

        viewModelScope.launch {
            val series = GripSeries(
                id = current.id,
                name = current.name.ifBlank { "Hand Grip Session" },
                notes = current.notes,
                createdAt = if (current.id == 0L) System.currentTimeMillis() else System.currentTimeMillis()
            )
            val sets = current.sets.mapIndexed { index, draftSet ->
                GripSet(
                    id = draftSet.originalId,
                    seriesId = current.id,
                    hand = draftSet.hand,
                    weightOrLevel = draftSet.weightOrLevel,
                    reps = draftSet.reps,
                    orderIndex = index,
                    isCompleted = true
                )
            }
            repository.saveSeriesWithSets(series, sets)
            _activeDraft.value = null

            // Start rest timer
            val defaultRest = userProfile.value.defaultRestSeconds.coerceAtLeast(30)
            startRestTimer(defaultRest)
        }
    }

    fun discardDraft() {
        _activeDraft.value = null
    }

    fun deleteSeries(seriesId: Long) {
        viewModelScope.launch {
            repository.deleteSeries(seriesId)
        }
    }

    // Quick Tap Squeeze Logging (Directly logs 1 or 5 reps to today's volume)
    fun logQuickSqueeze(hand: HandSide, reps: Int = 1) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
            val currentList = seriesList.value
            val todaySeries = currentList.firstOrNull {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.series.createdAt)) == todayKey
            }

            val defaultWeight = _gripperLocker.value.firstOrNull()?.ratingOrWeight ?: "45 kg"

            if (todaySeries != null) {
                // Add set to today's existing series
                val newSet = GripSet(
                    seriesId = todaySeries.series.id,
                    hand = hand,
                    weightOrLevel = defaultWeight,
                    reps = reps,
                    orderIndex = todaySeries.sets.size,
                    isCompleted = true
                )
                repository.saveSeriesWithSets(todaySeries.series, todaySeries.sets + newSet)
            } else {
                // Create a new session for today
                val newSeries = GripSeries(
                    name = "Daily Grip Session",
                    createdAt = now,
                    notes = "Quick squeeze workout"
                )
                val newSet = GripSet(
                    seriesId = 0,
                    hand = hand,
                    weightOrLevel = defaultWeight,
                    reps = reps,
                    orderIndex = 0,
                    isCompleted = true
                )
                repository.saveSeriesWithSets(newSeries, listOf(newSet))
            }
            triggerHapticFeedback(50)
        }
    }

    // Equipment Locker
    fun addGripperEquipment(name: String, rating: String, type: String) {
        val newGripper = GripperEquipment(
            id = UUID.randomUUID().toString(),
            name = name,
            ratingOrWeight = rating,
            type = type
        )
        _gripperLocker.value = _gripperLocker.value + newGripper
    }

    fun removeGripperEquipment(id: String) {
        _gripperLocker.value = _gripperLocker.value.filter { it.id != id }
    }

    // Profile Management
    fun updateProfileSettings(
        name: String,
        unit: String,
        goalReps: Int,
        rankTitle: String = userProfile.value.rankTitle,
        defaultRest: Int = userProfile.value.defaultRestSeconds
    ) {
        viewModelScope.launch {
            repository.updateProfile(name, unit, goalReps, rankTitle, defaultRest)
        }
    }

    fun updateAthleteName(name: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(name, current.preferredUnit, current.goalRepsPerDay, current.rankTitle, current.defaultRestSeconds)
        }
    }

    fun updateGoalReps(goalReps: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.name, current.preferredUnit, goalReps, current.rankTitle, current.defaultRestSeconds)
        }
    }

    fun updateUnit(unit: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.name, unit, current.goalRepsPerDay, current.rankTitle, current.defaultRestSeconds)
        }
    }

    // Analytics Horizon Selector
    fun selectHorizon(horizon: String) {
        _selectedHorizon.value = horizon
    }

    // Rest Timer Logic
    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _restTotalSeconds.value = seconds
        _restSecondsRemaining.value = seconds
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_restSecondsRemaining.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                if (_isTimerRunning.value) {
                    _restSecondsRemaining.value -= 1
                }
            }
            if (_restSecondsRemaining.value <= 0) {
                _isTimerRunning.value = false
                triggerHapticFeedback()
                _timerFinishedEvent.emit(Unit)
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
    }

    fun resumeTimer() {
        if (_restSecondsRemaining.value <= 0) {
            startRestTimer(_restTotalSeconds.value)
            return
        }
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_restSecondsRemaining.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                if (_isTimerRunning.value) {
                    _restSecondsRemaining.value -= 1
                }
            }
            if (_restSecondsRemaining.value <= 0) {
                _isTimerRunning.value = false
                triggerHapticFeedback()
                _timerFinishedEvent.emit(Unit)
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _restSecondsRemaining.value = 0
    }

    fun addTimerSeconds(delta: Int) {
        val newTime = (_restSecondsRemaining.value + delta).coerceIn(0, 600)
        _restSecondsRemaining.value = newTime
        if (newTime > _restTotalSeconds.value) {
            _restTotalSeconds.value = newTime
        }
    }

    private fun triggerHapticFeedback(durationMs: Long = 150) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration service is restricted or unavailable
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
