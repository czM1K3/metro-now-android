package dev.metronow.android.features.departure.data.api

import com.apollographql.apollo.ApolloClient
import dev.metronow.android.DeparturesQuery
import dev.metronow.android.features.departure.domain.Departure

class DepartureRemoteDataSource(private val apolloClient: ApolloClient) {
    suspend fun getDepartures(platformIds: List<String>): List<Departure> {
        val response = apolloClient
            .query(DeparturesQuery(platformIds))
            .execute()
        return response.data?.departures?.map { it.toDomain() } ?: emptyList()
    }
}
