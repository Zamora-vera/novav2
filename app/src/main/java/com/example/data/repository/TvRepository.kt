package com.example.data.repository

import com.example.data.api.TvApiService
import com.example.data.local.FavoriteChannelDao
import com.example.data.local.MostWatchedDao
import com.example.data.local.TvPreferences
import com.example.data.local.WatchHistoryDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class TvRepository(
    private val apiService: TvApiService,
    private val favoriteChannelDao: FavoriteChannelDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val mostWatchedDao: MostWatchedDao,
    private val favoriteShowDao: com.example.data.local.FavoriteShowDao,
    private val favoriteMovieDao: com.example.data.local.FavoriteMovieDao,
    private val continueWatchingDao: com.example.data.local.ContinueWatchingDao,
    private val preferences: TvPreferences
) {

    // --- DataStore Preferences ---
    val countryFlow: Flow<String> = preferences.countryFlow
    val languageFlow: Flow<String> = preferences.languageFlow
    val sessionIdFlow: Flow<String> = preferences.sessionIdFlow
    val lastAdTimestampFlow: Flow<Long> = preferences.lastAdTimestampFlow
    val introShownFlow: Flow<Boolean> = preferences.introShownFlow
    val playIntroPrefFlow: Flow<Boolean> = preferences.playIntroPrefFlow

    suspend fun saveCountry(countryCode: String) = preferences.saveCountry(countryCode)
    suspend fun saveLanguage(languageCode: String) = preferences.saveLanguage(languageCode)
    suspend fun saveSessionId(sessionId: String) = preferences.saveSessionId(sessionId)
    suspend fun saveLastAdTimestamp(timestamp: Long) = preferences.saveLastAdTimestamp(timestamp)
    suspend fun saveIntroShown(shown: Boolean) = preferences.saveIntroShown(shown)
    suspend fun savePlayIntroPref(playIntro: Boolean) = preferences.savePlayIntroPref(playIntro)
    suspend fun getOrGenerateSessionId(): String = preferences.getOrGenerateSessionId()

    // --- Room Local Database ---
    val allFavorites: Flow<List<LocalFavoriteChannel>> = favoriteChannelDao.getAllFavorites()
    val watchHistory: Flow<List<LocalWatchHistory>> = watchHistoryDao.getWatchHistory()
    val mostWatched: Flow<List<LocalMostWatched>> = mostWatchedDao.getMostWatched()
    val favoriteShows: Flow<List<LocalFavoriteShow>> = favoriteShowDao.getAllFavoriteShows()
    val favoriteMovies: Flow<List<LocalFavoriteMovie>> = favoriteMovieDao.getAllFavoriteMovies()
    val continueWatching: Flow<List<LocalContinueWatching>> = continueWatchingDao.getContinueWatching()

    suspend fun addFavorite(channel: TvChannel) {
        favoriteChannelDao.insertFavorite(
            LocalFavoriteChannel(
                id = channel.id,
                name = channel.name,
                logo = channel.logo,
                streamUrl = channel.resolvedStreamUrl,
                playbackType = channel.resolvedPlaybackType,
                youtubeVideoId = channel.youtube_video_id,
                youtubeEmbedUrl = channel.youtube_embed_url,
                addedAt = System.currentTimeMillis(),
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFavorite(channelId: String) {
        favoriteChannelDao.deleteFavoriteById(channelId)
    }

    fun observeIsFavorite(channelId: String): Flow<Boolean> {
        return favoriteChannelDao.observeIsFavorite(channelId)
    }

    suspend fun addToWatchHistory(channel: TvChannel) {
        val now = System.currentTimeMillis()
        // Save history
        watchHistoryDao.insertWatchHistory(
            LocalWatchHistory(
                id = channel.id,
                name = channel.name,
                logo = channel.logo,
                streamUrl = channel.resolvedStreamUrl,
                playbackType = channel.resolvedPlaybackType,
                youtubeVideoId = channel.youtube_video_id,
                youtubeEmbedUrl = channel.youtube_embed_url,
                lastWatchedAt = now
            )
        )

        // Increment most watched count
        val current = mostWatchedDao.getMostWatchedById(channel.id)
        if (current != null) {
            mostWatchedDao.incrementViewCount(channel.id, now)
        } else {
            mostWatchedDao.insertMostWatched(
                LocalMostWatched(
                    id = channel.id,
                    title = channel.name,
                    logo = channel.logo,
                    viewCount = 1,
                    lastWatchedAt = now,
                    country = channel.country,
                    language = channel.language
                )
            )
        }
    }

    suspend fun clearHistory() {
        watchHistoryDao.clearHistory()
    }

    // --- Remote API with Exception Handling ---

    suspend fun getAppConfig(): Result<TvAppConfigResponse> = safeApiCall {
        apiService.getAppConfig()
    }

    suspend fun getStatus(): Result<TvStatusResponse> = safeApiCall {
        apiService.getStatus()
    }

    suspend fun getHome(country: String, language: String): Result<TvHomeResponse> = safeApiCall {
        apiService.getHome(country = country, language = language)
    }

    suspend fun getChannels(country: String, language: String, category: String? = null): Result<TvChannelsResponse> = safeApiCall {
        apiService.getChannels(country = country, language = language, category = category)
    }

    suspend fun getChannel(id: String): Result<TvChannelDetailResponse> = safeApiCall {
        apiService.getChannel(id = id)
    }

    suspend fun getCategories(): Result<TvCategoriesResponse> = safeApiCall {
        apiService.getCategories()
    }

    suspend fun getCountries(): Result<TvCountriesResponse> = safeApiCall {
        apiService.getCountries()
    }

    suspend fun getLanguages(): Result<TvLanguagesResponse> = safeApiCall {
        apiService.getLanguages()
    }

    suspend fun getIntro(country: String, language: String): Result<TvIntroResponse> = safeApiCall {
        apiService.getIntro(country = country, language = language)
    }

    suspend fun getAds(
        screen: String,
        country: String,
        language: String? = null,
        channelId: String? = null
    ): Result<TvAdsResponse> = safeApiCall {
        apiService.getAds(screen = screen, country = country, language = language, channelId = channelId)
    }

    suspend fun trackAdImpression(adId: String, channelId: String?, screen: String, country: String) {
        val sessionId = getOrGenerateSessionId()
        val body = AdTrackingRequest(
            ad_id = adId,
            channel_id = channelId,
            screen = screen,
            session_id = sessionId,
            country = country
        )
        try {
            apiService.trackAdImpression(body)
        } catch (e: Exception) {
            // Silently swallow tracking errors
        }
    }

    suspend fun trackAdClick(adId: String, channelId: String?, screen: String, country: String) {
        val sessionId = getOrGenerateSessionId()
        val body = AdTrackingRequest(
            ad_id = adId,
            channel_id = channelId,
            screen = screen,
            session_id = sessionId,
            country = country
        )
        try {
            apiService.trackAdClick(body)
        } catch (e: Exception) {
            // Silently swallow tracking errors
        }
    }

    suspend fun getMovies(): Result<TvMoviesResponse> = safeApiCall {
        apiService.getMovies()
    }

    suspend fun searchMovies(query: String, limit: Int = 20): Result<TvMoviesResponse> = safeApiCall {
        apiService.searchMovies(query = query, limit = limit)
    }

    suspend fun getMoviesByGenre(genre: String): Result<TvMoviesResponse> = safeApiCall {
        apiService.getMoviesByGenre(genre = genre)
    }

    suspend fun getTrailerCache(): Result<TvTrailersResponse> = safeApiCall {
        apiService.getTrailerCache()
    }

    suspend fun getKinoCheckTrailers(language: String): Result<TvTrailersResponse> = safeApiCall {
        apiService.getKinoCheckTrailers(language = language)
    }

    // --- New API Endpoint mappings and Local Database helper operations ---

    suspend fun getContentConfig(): Result<TvContentConfigResponse> = safeApiCall {
        apiService.getContentConfig()
    }

    suspend fun getCountryContent(country: String): Result<TvCountryContentResponse> = safeApiCall {
        apiService.getCountryContent(country)
    }

    suspend fun countrySearch(country: String, query: String, limit: Int = 30): Result<TvSearchCombinedResponse> = safeApiCall {
        apiService.countrySearch(country, query, limit)
    }

    suspend fun getShows(): Result<TvShowsResponse> = safeApiCall {
        apiService.getShows()
    }

    suspend fun getShowDetail(id: String): Result<TvShowDetailResponse> = safeApiCall {
        apiService.getShowDetail(id)
    }

    suspend fun getShowEpisodes(id: String): Result<TvEpisodesResponse> = safeApiCall {
        apiService.getShowEpisodes(id)
    }

    suspend fun getTvMazeSchedule(country: String = "US"): Result<TvScheduleResponse> = safeApiCall {
        apiService.getTvMazeSchedule(country)
    }

    suspend fun searchShows(query: String, limit: Int = 20): Result<TvShowsResponse> = safeApiCall {
        apiService.searchShows(query, limit)
    }

    // Favorite Show Operations
    suspend fun addFavoriteShow(show: TvShow) {
        favoriteShowDao.insertFavoriteShow(
            LocalFavoriteShow(
                id = show.id,
                name = show.displayTitle,
                poster = show.displayImageUrl,
                description = show.displayDescription,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFavoriteShow(showId: String) {
        favoriteShowDao.deleteFavoriteShowById(showId)
    }

    fun observeIsFavoriteShow(showId: String): Flow<Boolean> {
        return favoriteShowDao.observeIsFavoriteShow(showId)
    }

    // Favorite Movie Operations
    suspend fun addFavoriteMovie(movie: TvMovie) {
        favoriteMovieDao.insertFavoriteMovie(
            LocalFavoriteMovie(
                id = movie.id,
                title = movie.title,
                poster = movie.displayImageUrl,
                description = movie.description,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFavoriteMovie(movieId: String) {
        favoriteMovieDao.deleteFavoriteMovieById(movieId)
    }

    fun observeIsFavoriteMovie(movieId: String): Flow<Boolean> {
        return favoriteMovieDao.observeIsFavoriteMovie(movieId)
    }

    // Continue Watching Operations
    suspend fun addContinueWatching(id: String, title: String, poster: String?, contentType: String, progress: Float = 0f) {
        continueWatchingDao.insertContinueWatching(
            LocalContinueWatching(
                id = id,
                title = title,
                poster = poster,
                contentType = contentType,
                progress = progress,
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeContinueWatching(id: String) {
        continueWatchingDao.deleteContinueWatchingById(id)
    }

    // Helper wrapper to handle networking or conversion exceptions elegantly
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (throwable: Throwable) {
            Result.failure(translateException(throwable))
        }
    }

    private fun translateException(throwable: Throwable): Exception {
        return when (throwable) {
            is IOException -> Exception("No hay conexión a internet. Verifique su red.")
            else -> Exception("Estamos mejorando la experiencia de Nova TV.")
        }
    }
}
