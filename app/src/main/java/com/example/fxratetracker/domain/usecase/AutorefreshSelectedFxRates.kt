package com.example.fxratetracker.domain.usecase

import android.util.Log
import arrow.core.Either
import com.example.fxratetracker.domain.repository.FxRateRepository
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * Create flow that emits current fx rate refresh state.
 * While flow is being collected fx rate will refresh each [period]
 * */
class AutorefreshSelectedFxRates(
    private val dispatcher: CoroutineDispatcher,
    private val period: Duration,
    private val fxRateRepository: FxRateRepository,
    private val selectedAssetsRepository: SelectedAssetsRepository,
) {

    sealed interface State {
        data object Stale : State
        data class Running(val refreshTime: LocalDateTime) : State
        data class Failed(val exception: Exception) : State
    }

    operator fun invoke(): Flow<State> = flow {
        emit(State.Stale)

        while (currentCoroutineContext().isActive) {
            val selectedCodes = selectedAssetsRepository.getSelectedAssets()

            when (val result = fxRateRepository.refreshFxRates(selectedCodes)) {
                is Either.Left -> {
                    emit(State.Failed(Exception(result.value.exception)))
                    break
                }

                is Either.Right -> {
                    emit(State.Running(LocalDateTime.now()))
                    delay(period)
                }
            }
        }
    }.catch { e ->
        Log.e("AutorefreshSelectedFxRates", "Fx rate autorefresh failed", e)
        emit(State.Failed(Exception(e)))
    }
        .flowOn(dispatcher)

}