package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

// --- Network Models ---

data class TvAppConfigResponse(
    val success: Boolean = true,
    val app: TvAppDetail = TvAppDetail()
)

data class TvAppConfig(
    val app: TvAppDetail,
    val maintenance: Boolean? = false,
    val maintenance_message: String? = null
)

data class TvAppDetail(
    val name: String? = null,
    val logo: String? = null,
    val splash: String? = null,
    val background: String? = null,
    val backgroundImage: String? = null,
    val primary_color: String? = null,
    val primaryColor: String? = null,
    val intro_enabled: Boolean? = null,
    val introEnabled: Boolean? = null,
    val ads_enabled: Boolean? = null,
    val showAds: Boolean? = null,
    val introVideoUrl: String? = null,
    val default_country: String? = null,
    val defaultCountry: String? = null,
    val default_language: String? = null,
    val defaultLanguage: String? = null,
    val countrySelectionRequired: Boolean? = null,
    val globalAdsFrequency: Int? = null,
    val maintenanceMode: Boolean? = null,
    val maintenanceMessage: String? = null,
    val maintenance: Boolean? = null,
    val maintenance_message: String? = null
) {
    val isIntroEnabled: Boolean get() = intro_enabled == true || introEnabled == true
    val areAdsEnabled: Boolean get() = ads_enabled == true || showAds == true
    val isMaintenance: Boolean get() = maintenance == true || maintenanceMode == true
    val maintenanceText: String? get() = maintenance_message ?: maintenanceMessage
    val requiresCountrySelection: Boolean get() = countrySelectionRequired == true || (defaultCountry ?: default_country).isNullOrBlank()
    val defaultCountryValue: String get() = defaultCountry ?: default_country ?: ""
    val defaultLanguageValue: String get() = defaultLanguage ?: default_language ?: ""
    val introVideo: String? get() = introVideoUrl
    val primaryColorValue: String? get() = primaryColor ?: primary_color
    val backgroundValue: String? get() = background ?: backgroundImage
}

data class TvStatusResponse(
    val success: Boolean = true,
    val status: String? = null,
    val message: String? = null
)

data class TvHomeResponse(
    val success: Boolean = true,
    val featured_channels: List<TvChannel>? = emptyList(),
    val categories: List<TvCategory>? = emptyList(),
    val movies: List<TvMovie>? = emptyList(),
    val trailers: List<TvTrailer>? = emptyList()
)

data class TvHomeData(
    val featured_channels: List<TvChannel>? = emptyList(),
    val categories: List<TvCategory>? = emptyList(),
    val countries: List<TvCountry>? = emptyList(),
    val languages: List<TvLanguage>? = emptyList(),
    val movies: List<TvMovie>? = emptyList(),
    val trailers: List<TvTrailer>? = emptyList()
)

data class TvChannelsResponse(
    val success: Boolean = true,
    val total: Int? = null,
    val channels: List<TvChannel> = emptyList()
)

data class TvChannelDetailResponse(
    val success: Boolean = true,
    val channel: TvChannel = TvChannel()
)

data class TvCategoriesResponse(
    val success: Boolean = true,
    val categories: List<TvCategory> = emptyList()
)

data class TvCountriesResponse(
    val success: Boolean = true,
    val countries: List<TvCountry> = emptyList()
)

data class TvLanguagesResponse(
    val success: Boolean = true,
    val languages: List<TvLanguage> = emptyList()
)

data class TvChannel(
    val id: String = "",
    val name: String = "Canal",
    val slug: String? = null,
    val logo: String? = null,
    val stream_url: String? = null,
    val streamUrl: String? = null,
    val playback_type: String? = null,
    val type: String? = null,
    val youtube_video_id: String? = null,
    val youtube_embed_url: String? = null,
    val is_featured: Boolean? = null,
    val isFeatured: Boolean? = null,
    val category: String? = null,
    val country: String? = null,
    val language: String? = null,
    val description: String? = null,
    val status_label: String? = null
) {
    val resolvedStreamUrl: String? get() = stream_url ?: streamUrl
    val resolvedPlaybackType: String? get() = playback_type ?: type ?: "hls"
    val isFeaturedValue: Boolean get() = is_featured == true || isFeatured == true
}

data class TvCategory(
    val id: String = "",
    val name: String = "Categoría",
    val slug: String? = null,
    val icon: String? = null
)

data class TvCountry(
    @Json(name = "country_code") val code: String = "",
    @Json(name = "country_name") val name: String = "",
    val flag: String? = null,
    val language: String? = null,
    val app_language: String? = null,
    val currency: String? = null,
    val timezone: String? = null,
    val fallback_languages: List<String>? = emptyList(),
    val sections: List<TvContentSection>? = emptyList()
)

data class TvLanguage(
    val code: String = "",
    val name: String = ""
)

data class TvIntroResponse(
    val success: Boolean = true,
    val intro: TvIntro? = null
)

data class TvIntro(
    val id: String? = null,
    val video_url: String? = null,
    val file_url: String? = null,
    val fileUrl: String? = null,
    val skip_after_seconds: Int? = null,
    val skipAfterSeconds: Int? = null,
    val active: Boolean? = false
) {
    val videoUrlResolved: String? get() = video_url ?: file_url ?: fileUrl
    val skipSeconds: Int get() = skip_after_seconds ?: skipAfterSeconds ?: 5
}

data class TvAdsResponse(
    val success: Boolean = true,
    val ads: List<TvAd>? = emptyList()
)

data class TvAd(
    val id: String = "",
    val name: String? = null,
    val client: String? = null,
    val type: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val targetUrl: String? = null,
    val shortText: String? = null,
    val buttonText: String? = null,
    val platform: String? = null,
    val frequencyMinutes: Int? = null,
    val maxImpressionsPerSession: Int? = null,
    val maxImpressionsPerDay: Int? = null,
    val is_active: Boolean? = true,
    val status: String? = null
) {
    val title: String? get() = name
    val media_url: String? get() = imageUrl ?: videoUrl
    val click_url: String? get() = targetUrl
    val frequency_minutes: Int? get() = frequencyMinutes
    val skip_after_seconds: Int? get() = 5
    val max_impressions: Int? get() = maxImpressionsPerSession ?: 10
    val isActiveValue: Boolean get() = is_active != false && status != "inactive"
}

data class TvMoviesResponse(
    val success: Boolean = true,
    val movies: List<TvMovie>? = emptyList(),
    val saved_count: Int? = null
)

data class TvMovie(
    val id: String = "",
    val title: String = "Película",
    val description: String? = null,
    val overview: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val image_url: String? = null,
    val year: String? = null,
    val genre: List<String>? = emptyList(),
    val genres: List<String>? = emptyList(),
    val stars: Double? = null,
    val rating: Double? = null,
    val stream_url: String? = null,
    val has_trailer: Boolean? = false,
    val trailer_youtube_id: String? = null,
    val youtube_video_id: String? = null,
    val youtube_embed_url: String? = null,
    val watch_url: String? = null,
    val playback_type: String? = null,
    val source: String? = null,
    val type: String? = null
) {
    val displayImageUrl: String? get() = image_url ?: poster ?: backdrop
    val displayYoutubeId: String? get() = youtube_video_id ?: trailer_youtube_id
    val displayDescription: String? get() = description ?: overview
    val displayGenres: List<String> get() = genre ?: genres ?: emptyList()
    val displayRating: Double? get() = stars ?: rating
}

data class TvTrailersResponse(
    val success: Boolean = true,
    val trailers: List<TvTrailer>? = emptyList()
)

data class TvTrailer(
    val id: String = "",
    val title: String = "Trailer",
    val thumbnail: String? = null,
    val image_url: String? = null,
    val youtube_video_id: String? = null,
    val youtube_embed_url: String? = null,
    val language: String? = null,
    val category: String? = null,
    val categories: List<String>? = emptyList(),
    val release_date: String? = null
) {
    val displayImageUrl: String? get() = thumbnail ?: image_url
}

data class WatchmodeSearchResponse(
    val success: Boolean = true,
    val results: List<WatchmodeResult>? = emptyList()
)

data class WatchmodeResult(
    val id: String? = null,
    val name: String? = null,
    val year: String? = null,
    val type: String? = null,
    val sources: List<WatchmodeSource>? = emptyList()
)

data class WatchmodeSource(
    val name: String? = null,
    val type: String? = null,
    val web_url: String? = null
)

data class AdTrackingRequest(
    val ad_id: String,
    val channel_id: String? = null,
    val screen: String,
    val platform: String = "android_tv",
    val session_id: String,
    val country: String
)

data class BasicResponse(
    val success: Boolean = true,
    val message: String? = null
)

// --- Country config/content models matching Nova TV backend JSON ---

class TvContentConfigResponse(
    val success: Boolean = true,
    val config: TvContentConfig? = null,
    @Json(name = "countries") val directCountries: List<TvCountry>? = null,
    @Json(name = "languages") val directLanguages: List<TvLanguage>? = null
) {
    val countries: List<TvCountry>? get() = directCountries ?: config?.countries ?: emptyList()
    val languages: List<TvLanguage>? get() = directLanguages ?: config?.derivedLanguages ?: emptyList()
}

data class TvContentConfig(
    val app: TvContentApp? = null,
    val providers: Map<String, TvProviderConfig>? = emptyMap(),
    val countries: List<TvCountry>? = emptyList()
) {
    val derivedLanguages: List<TvLanguage>
        get() = countries.orEmpty()
            .mapNotNull { it.app_language ?: it.language?.substringBefore("-") }
            .distinct()
            .map { code ->
                TvLanguage(
                    code = code,
                    name = when (code.lowercase()) {
                        "es" -> "Español"
                        "it" -> "Italiano"
                        "fr" -> "Français"
                        "ht" -> "Kreyòl"
                        "en" -> "English"
                        else -> code.uppercase()
                    }
                )
            }
}

data class TvContentApp(
    val name: String? = null,
    val version: String? = null,
    val default_country: String? = null,
    val country_selection_required: Boolean? = null,
    val legal_notice: String? = null
)

data class TvProviderConfig(
    val name: String? = null,
    val requires_api_key: Boolean? = null,
    val base_url: String? = null
)

data class TvCountryContentResponse(
    val success: Boolean = true,
    val country: String? = null,
    val country_config: TvCountry? = null,
    val content: TvCountryContentData? = null,
    @Json(name = "featured") val directFeatured: List<TvMovie>? = null,
    @Json(name = "movies") val directMovies: List<TvMovie>? = null,
    @Json(name = "shows") val directShows: List<TvShow>? = null,
    @Json(name = "series") val directSeries: List<TvShow>? = null,
    @Json(name = "novelas") val directNovelas: List<TvShow>? = null,
    @Json(name = "trailers") val directTrailers: List<TvTrailer>? = null,
    @Json(name = "free_content") val directFreeContent: List<TvMovie>? = null
) {
    private val sections: List<TvContentSection> get() = content?.sections ?: emptyList()

    val featured: List<TvMovie>? get() = directFeatured ?: movies
    val movies: List<TvMovie>? get() = directMovies ?: sections
        .filter { it.type == "movie" }
        .flatMap { it.items.orEmpty().map { item -> item.toMovie() } }
    val shows: List<TvShow>? get() = directShows ?: sections
        .filter { it.type == "tv" || it.type == "show" }
        .flatMap { it.items.orEmpty().map { item -> item.toShow() } }
    val series: List<TvShow>? get() = directSeries ?: shows
    val novelas: List<TvShow>? get() = directNovelas ?: sections
        .filter { it.id.contains("novela", true) || it.id.contains("soap", true) || it.title.contains("novela", true) || it.title.contains("fiction", true) }
        .flatMap { it.items.orEmpty().map { item -> item.toShow() } }
    val trailers: List<TvTrailer>? get() = directTrailers ?: sections
        .filter { it.type == "trailer" }
        .flatMap { it.items.orEmpty().map { item -> item.toTrailer() } }
    val free_content: List<TvMovie>? get() = directFreeContent ?: sections
        .filter { it.type == "archive_movies" || it.id.contains("archive", true) }
        .flatMap { it.items.orEmpty().map { item -> item.toMovie() } }
}

data class TvCountryContentData(
    val country_code: String? = null,
    val country_name: String? = null,
    val language: String? = null,
    val sections: List<TvContentSection>? = emptyList(),
    val updated_at: String? = null
)

data class TvContentSection(
    val id: String = "",
    val title: String = "",
    val type: String? = null,
    val provider: String? = null,
    val items: List<TvContentItem>? = emptyList(),
    val total_results: Int? = null,
    val total_pages: Int? = null,
    val page: Int? = null
)

data class TvContentItem(
    val id: String = "",
    val provider: String? = null,
    val provider_id: String? = null,
    val title: String? = null,
    val name: String? = null,
    val original_title: String? = null,
    val original_name: String? = null,
    val type: String? = null,
    val country: String? = null,
    val language: String? = null,
    val overview: String? = null,
    val description: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val image_url: String? = null,
    val thumbnail: String? = null,
    val rating: Double? = null,
    val stars: Double? = null,
    val year: String? = null,
    val genres: List<String>? = emptyList(),
    val genre: List<String>? = emptyList(),
    val video_url: String? = null,
    val stream_url: String? = null,
    val playback_type: String? = null,
    val youtube_video_id: String? = null,
    val youtube_embed_url: String? = null,
    val watch_url: String? = null,
    val section_id: String? = null,
    val section_title: String? = null
) {
    private val safeTitle: String get() = title ?: name ?: original_title ?: original_name ?: "Contenido"

    fun toMovie(): TvMovie = TvMovie(
        id = id.ifBlank { safeTitle },
        title = safeTitle,
        description = description ?: overview,
        overview = overview,
        poster = poster,
        backdrop = backdrop,
        image_url = image_url ?: poster ?: thumbnail,
        year = year,
        genre = genre ?: genres,
        genres = genres ?: genre,
        stars = stars ?: rating,
        rating = rating ?: stars,
        stream_url = stream_url ?: video_url,
        youtube_video_id = youtube_video_id,
        youtube_embed_url = youtube_embed_url,
        watch_url = watch_url,
        playback_type = playback_type ?: "metadata_only",
        source = provider,
        type = type ?: "movie"
    )

    fun toShow(): TvShow = TvShow(
        id = id.ifBlank { safeTitle },
        name = safeTitle,
        title = safeTitle,
        description = description ?: overview,
        summary = overview,
        image_url = image_url ?: poster ?: thumbnail,
        poster = poster ?: image_url,
        genres = genres ?: genre,
        genre = genre ?: genres,
        rating = rating ?: stars,
        playback_type = playback_type ?: "metadata_only",
        youtube_video_id = youtube_video_id,
        watch_url = watch_url,
        stream_url = stream_url ?: video_url,
        type = type
    )

    fun toTrailer(): TvTrailer = TvTrailer(
        id = id.ifBlank { safeTitle },
        title = safeTitle,
        thumbnail = thumbnail ?: image_url ?: poster,
        image_url = image_url ?: thumbnail ?: poster,
        youtube_video_id = youtube_video_id,
        youtube_embed_url = youtube_embed_url,
        language = language,
        category = type,
        categories = genre ?: genres
    )

    fun toChannel(): TvChannel = TvChannel(
        id = id.ifBlank { safeTitle },
        name = safeTitle,
        logo = image_url ?: poster ?: thumbnail,
        stream_url = stream_url ?: video_url,
        playback_type = playback_type ?: "hls",
        youtube_video_id = youtube_video_id,
        youtube_embed_url = youtube_embed_url,
        country = country,
        language = language,
        category = section_title ?: type
    )
}

class TvSearchCombinedResponse(
    val success: Boolean = true,
    val country: String? = null,
    val query: String? = null,
    val results: List<TvContentItem>? = emptyList(),
    @Json(name = "channels") val directChannels: List<TvChannel>? = null,
    @Json(name = "movies") val directMovies: List<TvMovie>? = null,
    @Json(name = "shows") val directShows: List<TvShow>? = null,
    @Json(name = "series") val directSeries: List<TvShow>? = null,
    @Json(name = "novelas") val directNovelas: List<TvShow>? = null,
    @Json(name = "trailers") val directTrailers: List<TvTrailer>? = null
) {
    val channels: List<TvChannel>? get() = directChannels ?: results.orEmpty()
        .filter { it.type == "channel" || !it.stream_url.isNullOrBlank() }
        .map { it.toChannel() }
    val movies: List<TvMovie>? get() = directMovies ?: results.orEmpty()
        .filter { it.type == "movie" || it.provider == "tmdb" }
        .map { it.toMovie() }
    val shows: List<TvShow>? get() = directShows ?: directSeries ?: directNovelas ?: results.orEmpty()
        .filter { it.type == "tv" || it.type == "show" || it.type == "series" }
        .map { it.toShow() }
    val series: List<TvShow>? get() = directSeries ?: shows
    val novelas: List<TvShow>? get() = directNovelas ?: results.orEmpty()
        .filter { it.section_title?.contains("novela", true) == true }
        .map { it.toShow() }
    val trailers: List<TvTrailer>? get() = directTrailers ?: results.orEmpty()
        .filter { !it.youtube_video_id.isNullOrBlank() }
        .map { it.toTrailer() }
}

data class TvShowsResponse(
    val success: Boolean = true,
    val shows: List<TvShow>? = emptyList(),
    val saved_count: Int? = null
)

data class TvShowDetailResponse(
    val success: Boolean = true,
    val show: TvShow = TvShow(),
    val episodes: List<TvEpisode>? = emptyList()
)

data class TvShow(
    val id: String = "",
    val name: String = "Serie",
    val title: String? = null,
    val description: String? = null,
    val summary: String? = null,
    val image_url: String? = null,
    val poster: String? = null,
    val genres: List<String>? = emptyList(),
    val genre: List<String>? = emptyList(),
    val rating: Double? = null,
    val status: String? = null,
    val playback_type: String? = null,
    val youtube_video_id: String? = null,
    val watch_url: String? = null,
    val stream_url: String? = null,
    val type: String? = null,
    val premiered: String? = null
) {
    val displayTitle: String get() = title ?: name
    val displayImageUrl: String? get() = image_url ?: poster
    val displayDescription: String? get() = description ?: summary
}

data class TvEpisodesResponse(
    val success: Boolean = true,
    val episodes: List<TvEpisode>? = emptyList()
)

data class TvScheduleResponse(
    val success: Boolean = true,
    val country: String? = null,
    val date: String? = null,
    val schedule: List<TvEpisode>? = emptyList()
)

data class TvEpisode(
    val id: String = "",
    val name: String = "Episodio",
    val title: String? = null,
    val season: Int? = 1,
    val number: Int? = 1,
    val summary: String? = null,
    val description: String? = null,
    val image_url: String? = null,
    val airdate: String? = null,
    val stream_url: String? = null,
    val show: TvShow? = null
) {
    val displayTitle: String get() = title ?: name
}

// --- Local Database Entities ---

@Entity(tableName = "favorite_channels")
data class LocalFavoriteChannel(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String?,
    val streamUrl: String?,
    val playbackType: String?,
    val youtubeVideoId: String?,
    val youtubeEmbedUrl: String?,
    val addedAt: Long,
    val lastWatchedAt: Long
)

@Entity(tableName = "watch_history")
data class LocalWatchHistory(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String?,
    val streamUrl: String?,
    val playbackType: String?,
    val youtubeVideoId: String?,
    val youtubeEmbedUrl: String?,
    val lastWatchedAt: Long,
    val minutesPlayed: Int = 0
)

@Entity(tableName = "most_watched_channels")
data class LocalMostWatched(
    @PrimaryKey val id: String,
    val title: String,
    val logo: String?,
    val viewCount: Int = 0,
    val lastWatchedAt: Long,
    val country: String?,
    val language: String?
)

@Entity(tableName = "favorite_shows")
data class LocalFavoriteShow(
    @PrimaryKey val id: String,
    val name: String,
    val poster: String?,
    val description: String?,
    val addedAt: Long
)

@Entity(tableName = "favorite_movies")
data class LocalFavoriteMovie(
    @PrimaryKey val id: String,
    val title: String,
    val poster: String?,
    val description: String?,
    val addedAt: Long
)

@Entity(tableName = "continue_watching")
data class LocalContinueWatching(
    @PrimaryKey val id: String,
    val title: String,
    val poster: String?,
    val contentType: String,
    val progress: Float = 0f,
    val lastWatchedAt: Long
)
