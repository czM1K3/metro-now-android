package dev.metronow.android.core.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Timer flows. Both emit immediately on collection and then once per interval.
 */
class UpdateFlow(
    val update: Flow<Unit>,
    val reFetch: Flow<Unit>,
) {
    constructor(config: RefreshConfig = RefreshConfig()) : this(
        update = ticker(config.tickMillis),
        reFetch = ticker(config.reFetchMillis),
    )

    companion object {
        fun ticker(periodMillis: Long): Flow<Unit> = flow {
            while (true) {
                emit(Unit)
                delay(periodMillis)
            }
        }
    }
}
