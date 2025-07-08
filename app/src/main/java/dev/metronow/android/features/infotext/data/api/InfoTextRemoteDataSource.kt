package dev.metronow.android.features.infotext.data.api

import com.apollographql.apollo.ApolloClient
import dev.metronow.android.InfoTextsQuery
import dev.metronow.android.features.infotext.domain.InfoText

class InfoTextRemoteDataSource(private val apolloClient: ApolloClient) {
    suspend fun getInfoTexts(): List<InfoText> {
        val response = apolloClient
            .query(InfoTextsQuery())
            .execute()
        return response.data?.infotexts?.map { it.toDomain() } ?: emptyList()
    }
}
