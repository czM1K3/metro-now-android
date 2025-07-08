package dev.metronow.android.features.stop.data.api

import com.apollographql.apollo.ApolloClient
import dev.metronow.android.StopsQuery
import dev.metronow.android.features.stop.domain.Stop

class StopRemoteDataSource(private val apolloClient: ApolloClient) {
    suspend fun getStops(): List<Stop> {
        val response = apolloClient
            .query(StopsQuery())
            .execute()
        return response.data?.stops?.map { it.toDomain() } ?: emptyList()
    }
}
