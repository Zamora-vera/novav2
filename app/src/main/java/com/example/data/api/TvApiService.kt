package com.example.data.api

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface TvApiService {

    @GET("/api/tv/app-config")
    suspend fun getAppConfig(): TvAppConfigResponse

    @GET("/api/tv/status")
    suspend fun getStatus(): TvStatusResponse

    @GET("/api/tv/home")
    suspend fun getHome(
        @Query("country") country: String,
        @Query("language") language: String,
        @Query("platform") platform: String = "android_tv"
    ): TvHomeResponse

    @GET("/api/tv/channels")
    suspend fun getChannels(
        @Query("country") country: String,
        @Query("language") language: String? = null,
        @Query("category") category: String? = null
    ): TvChannelsResponse

    @GET("/api/tv/channels/{id}")
    suspend fun getChannel(
        @Path("id") id: String,
        @Query("platform") platform: String = "android_tv"
    ): TvChannelDetailResponse

    @GET("/api/tv/categories")
    suspend fun getCategories(): TvCategoriesResponse

    @GET("/api/tv/countries")
    suspend fun getCountries(): TvCountriesResponse

    @GET("/api/tv/languages")
    suspend fun getLanguages(): TvLanguagesResponse

    @GET("/api/tv/intro")
    suspend fun getIntro(
        @Query("platform") platform: String = "android_tv",
        @Query("country") country: String,
        @Query("language") language: String
    ): TvIntroResponse

    @GET("/api/tv/ads")
    suspend fun getAds(
        @Query("screen") screen: String,
        @Query("platform") platform: String = "android_tv",
        @Query("country") country: String,
        @Query("language") language: String? = null,
        @Query("channel_id") channelId: String? = null
    ): TvAdsResponse

    @POST("/api/tv/ads/impression")
    suspend fun trackAdImpression(@Body body: AdTrackingRequest): BasicResponse

    @POST("/api/tv/ads/click")
    suspend fun trackAdClick(@Body body: AdTrackingRequest): BasicResponse

    @GET("/api/tv/movies")
    suspend fun getMovies(): TvMoviesResponse

    @GET("/api/tv/search/movies")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): TvMoviesResponse

    @GET("/api/tv/movies/genre/{genre}")
    suspend fun getMoviesByGenre(@Path("genre") genre: String): TvMoviesResponse

    @GET("/api/tv/trailers/cache")
    suspend fun getTrailerCache(): TvTrailersResponse

    @GET("/api/tv/trailers/kinocheck")
    suspend fun getKinoCheckTrailers(
        @Query("language") language: String,
        @Query("limit") limit: Int = 20
    ): TvTrailersResponse

    // --- New API endpoints for Country Selection, Country Content, Series, Novelas, & TVMaze ---

    @GET("/api/tv/content-config")
    suspend fun getContentConfig(): TvContentConfigResponse

    @GET("/api/tv/country-content")
    suspend fun getCountryContent(
        @Query("country") country: String
    ): TvCountryContentResponse

    @GET("/api/tv/country-search")
    suspend fun countrySearch(
        @Query("country") country: String,
        @Query("query") query: String,
        @Query("limit") limit: Int = 30
    ): TvSearchCombinedResponse

    @GET("/api/tv/shows")
    suspend fun getShows(): TvShowsResponse

    @GET("/api/tv/shows/{id}")
    suspend fun getShowDetail(@Path("id") id: String): TvShowDetailResponse

    @GET("/api/tv/shows/{id}/episodes")
    suspend fun getShowEpisodes(@Path("id") id: String): TvEpisodesResponse

    @GET("/api/tv/tvmaze/schedule")
    suspend fun getTvMazeSchedule(
        @Query("country") country: String = "US"
    ): TvScheduleResponse

    @GET("/api/tv/search/shows")
    suspend fun searchShows(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): TvShowsResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://delivery.allsender.tech"

    private val moshi = Moshi.Builder()
        .add(TolerantJsonAdapters())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: TvApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TvApiService::class.java)
    }

    fun fullMediaUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        return if (path.startsWith("http")) path else BASE_URL + path
    }
}
