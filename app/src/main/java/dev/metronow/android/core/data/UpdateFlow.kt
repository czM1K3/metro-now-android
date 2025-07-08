package dev.metronow.android.core.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateFlow(
    val update: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    },
    val reFetch: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(10_000)
        }
    },
)
