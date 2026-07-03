package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.local.TvPreferences
import com.example.data.model.*
import com.example.data.repository.TvRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SplashUiState {
    object Idle : SplashUiState
    object Loading : SplashUiState
    data class Success(val config: TvAppConfig) : SplashUiState
    data class Maintenance(val message: String) : SplashUiState
    data class Error(val message: String) : SplashUiState
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val homeData: TvHomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface ChannelsUiState {
    object Loading : ChannelsUiState
    data class Success(val channels: List<TvChannel>) : ChannelsUiState
    data class Error(val message: String) : ChannelsUiState
}

sealed interface MoviesUiState {
    object Loading : MoviesUiState
    data class Success(val movies: List<TvMovie>) : MoviesUiState
    data class Error(val message: String) : MoviesUiState
}

sealed interface TrailersUiState {
    object Loading : TrailersUiState
    data class Success(val trailers: List<TvTrailer>) : TrailersUiState
    data class Error(val message: String) : TrailersUiState
}

sealed interface PlayerUiState {
    object Idle : PlayerUiState
    object Loading : PlayerUiState
    data class Success(val channel: TvChannel, val ad: TvAd? = null) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TvRepository

    // Session-based ad impressions counter
    private var sessionImpressionsCount = 0

    init {
        val database = AppDatabase.getDatabase(application)
        val apiService = RetrofitClient.apiService
        val preferences = TvPreferences(application)
        repository = TvRepository(
            apiService = apiService,
            favoriteChannelDao = database.favoriteChannelDao(),
            watchHistoryDao = database.watchHistoryDao(),
            mostWatchedDao = database.mostWatchedDao(),
            favoriteShowDao = database.favoriteShowDao(),
            favoriteMovieDao = database.favoriteMovieDao(),
            continueWatchingDao = database.continueWatchingDao(),
            preferences = preferences
        )
        loadContentConfig()
    }

    // --- State Flows ---

    // Preferences & Settings
    val selectedCountry: StateFlow<String> = repository.countryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val selectedLanguage: StateFlow<String> = repository.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "es")

    val playIntroPref: StateFlow<Boolean> = repository.playIntroPrefFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val introShown: StateFlow<Boolean> = repository.introShownFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Room Streams
    val favorites: StateFlow<List<LocalFavoriteChannel>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<LocalWatchHistory>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostWatched: StateFlow<List<LocalMostWatched>> = repository.mostWatched
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteShows: StateFlow<List<LocalFavoriteShow>> = repository.favoriteShows
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMovies: StateFlow<List<LocalFavoriteMovie>> = repository.favoriteMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<LocalContinueWatching>> = repository.continueWatching
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Network UI States
    private val _splashState = MutableStateFlow<SplashUiState>(SplashUiState.Idle)
    val splashState: StateFlow<SplashUiState> = _splashState.asStateFlow()

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _channelsState = MutableStateFlow<ChannelsUiState>(ChannelsUiState.Loading)
    val channelsState: StateFlow<ChannelsUiState> = _channelsState.asStateFlow()

    private val _moviesState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val moviesState: StateFlow<MoviesUiState> = _moviesState.asStateFlow()

    private val _searchMoviesState = MutableStateFlow<MoviesUiState>(MoviesUiState.Success(emptyList()))
    val searchMoviesState: StateFlow<MoviesUiState> = _searchMoviesState.asStateFlow()

    private val _trailersState = MutableStateFlow<TrailersUiState>(TrailersUiState.Loading)
    val trailersState: StateFlow<TrailersUiState> = _trailersState.asStateFlow()

    private val _playerState = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    // Additional sealed states for Country Selection, TV Shows and search
    sealed interface CountryContentUiState {
        object Loading : CountryContentUiState
        data class Success(val data: TvCountryContentResponse) : CountryContentUiState
        data class Error(val message: String) : CountryContentUiState
    }

    sealed interface ShowDetailUiState {
        object Loading : ShowDetailUiState
        data class Success(val show: TvShow, val episodes: List<TvEpisode>) : ShowDetailUiState
        data class Error(val message: String) : ShowDetailUiState
    }

    sealed interface CombinedSearchUiState {
        object Idle : CombinedSearchUiState
        object Loading : CombinedSearchUiState
        data class Success(val result: TvSearchCombinedResponse) : CombinedSearchUiState
        data class Error(val message: String) : CombinedSearchUiState
    }

    private val _countryContentState = MutableStateFlow<CountryContentUiState>(CountryContentUiState.Loading)
    val countryContentState: StateFlow<CountryContentUiState> = _countryContentState.asStateFlow()

    private val _showDetailState = MutableStateFlow<ShowDetailUiState>(ShowDetailUiState.Loading)
    val showDetailState: StateFlow<ShowDetailUiState> = _showDetailState.asStateFlow()

    private val _combinedSearchState = MutableStateFlow<CombinedSearchUiState>(CombinedSearchUiState.Idle)
    val combinedSearchState: StateFlow<CombinedSearchUiState> = _combinedSearchState.asStateFlow()

    // Config cache
    private val _appConfig = MutableStateFlow<TvAppConfig?>(null)
    val appConfig: StateFlow<TvAppConfig?> = _appConfig.asStateFlow()

    // Intro config
    private val _introVideo = MutableStateFlow<TvIntro?>(null)
    val introVideo: StateFlow<TvIntro?> = _introVideo.asStateFlow()

    // Available countries/languages cache for configuration screens
    val countriesList = MutableStateFlow<List<TvCountry>>(emptyList())
    val languagesList = MutableStateFlow<List<TvLanguage>>(emptyList())
    val categoriesList = MutableStateFlow<List<TvCategory>>(emptyList())

    // Active Home Banner Ad
    private val _homeBannerAd = MutableStateFlow<TvAd?>(null)
    val homeBannerAd: StateFlow<TvAd?> = _homeBannerAd.asStateFlow()

    // Active Pre-roll Ad
    private val _playerPrerollAd = MutableStateFlow<TvAd?>(null)
    val playerPrerollAd: StateFlow<TvAd?> = _playerPrerollAd.asStateFlow()

    init {
        // Automatically reload content whenever country or language changes
        viewModelScope.launch {
            combine(selectedCountry, selectedLanguage) { c, l -> Pair(c, l) }
                .collectLatest { (country, language) ->
                    if (country.isNotEmpty()) {
                        loadHomeContent(country, language)
                        loadChannelsContent(country, language)
                        loadCountryContent(country)
                    }
                    loadMoviesAndTrailers(language)
                    loadStaticLists()
                }
        }
    }

    // --- Actions ---

    fun loadAppConfig() {
        viewModelScope.launch {
            _splashState.value = SplashUiState.Loading
            repository.getAppConfig().fold(
                onSuccess = { response ->
                    val appDetail = response.app
                    val config = TvAppConfig(
                        app = appDetail,
                        maintenance = appDetail.isMaintenance,
                        maintenance_message = appDetail.maintenanceText
                    )
                    _appConfig.value = config
                    
                    if (config.maintenance == true) {
                        _splashState.value = SplashUiState.Maintenance(
                            config.maintenance_message ?: "Nova TV está en mantenimiento. Estamos mejorando la experiencia."
                        )
                    } else {
                        // Check if intro is enabled
                        if (config.app.isIntroEnabled && playIntroPref.value) {
                            // Cargar intro
                            loadIntroConfig()
                        } else {
                            _splashState.value = SplashUiState.Success(config)
                        }
                    }
                },
                onFailure = { error ->
                    // Robust fallback to let the app open gracefully even when offline or with network issues
                    val fallbackAppDetail = TvAppDetail(
                        name = "Nova TV",
                        logo = null,
                        splash = null,
                        background = null,
                        primary_color = "#FF4D00",
                        intro_enabled = false,
                        ads_enabled = false,
                        maintenance = false,
                        maintenance_message = null
                    )
                    val fallbackConfig = TvAppConfig(
                        app = fallbackAppDetail,
                        maintenance = false,
                        maintenance_message = null
                    )
                    _appConfig.value = fallbackConfig
                    _splashState.value = SplashUiState.Success(fallbackConfig)
                }
            )
        }
    }

    private fun loadIntroConfig() {
        viewModelScope.launch {
            val country = selectedCountry.value
            val lang = selectedLanguage.value
            repository.getIntro(country = country, language = lang).fold(
                onSuccess = { response ->
                    val intro = response.intro
                    if (intro != null && intro.active == true && !intro.videoUrlResolved.isNullOrBlank()) {
                        _introVideo.value = intro
                    }
                    // Config load successful regardless of whether video actually exists
                    _appConfig.value?.let {
                        _splashState.value = SplashUiState.Success(it)
                    }
                },
                onFailure = { _ ->
                    // Fail gracefully, go directly to Home (Skip intro)
                    val currentConfig = _appConfig.value
                    if (currentConfig != null) {
                        _splashState.value = SplashUiState.Success(currentConfig)
                    }
                }
            )
        }
    }

    fun loadHomeContent(country: String = selectedCountry.value, language: String = selectedLanguage.value) {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            repository.getHome(country = country, language = language).fold(
                onSuccess = { response ->
                    val homeData = TvHomeData(
                        featured_channels = response.featured_channels ?: emptyList(),
                        categories = response.categories ?: emptyList(),
                        movies = response.movies ?: emptyList(),
                        trailers = response.trailers ?: emptyList()
                    )
                    _homeState.value = HomeUiState.Success(homeData)
                    // Try to load home ads if ads are enabled
                    if (_appConfig.value?.app?.areAdsEnabled == true) {
                        loadHomeAds(country)
                    }
                },
                onFailure = { error ->
                    _homeState.value = HomeUiState.Error(error.message ?: "No hay datos para mostrar.")
                }
            )
        }
    }

    fun loadChannelsContent(country: String = selectedCountry.value, language: String = selectedLanguage.value) {
        viewModelScope.launch {
            _channelsState.value = ChannelsUiState.Loading
            repository.getChannels(country = country, language = language).fold(
                onSuccess = { response ->
                    _channelsState.value = ChannelsUiState.Success(response.channels)
                },
                onFailure = { error ->
                    _channelsState.value = ChannelsUiState.Error(error.message ?: "No hay canales disponibles por el momento.")
                }
            )
        }
    }

    fun loadMoviesAndTrailers(language: String = selectedLanguage.value) {
        viewModelScope.launch {
            _moviesState.value = MoviesUiState.Loading
            repository.getMovies().fold(
                onSuccess = { response ->
                    _moviesState.value = MoviesUiState.Success(response.movies ?: emptyList())
                },
                onFailure = { error ->
                    _moviesState.value = MoviesUiState.Error(error.message ?: "No hay películas disponibles por el momento.")
                }
            )

            _trailersState.value = TrailersUiState.Loading
            repository.getKinoCheckTrailers(language).fold(
                onSuccess = { response ->
                    _trailersState.value = TrailersUiState.Success(response.trailers ?: emptyList())
                },
                onFailure = {
                    // Fallback to cache if kinocheck fails
                    repository.getTrailerCache().fold(
                        onSuccess = { cacheResponse ->
                            _trailersState.value = TrailersUiState.Success(cacheResponse.trailers ?: emptyList())
                        },
                        onFailure = { error ->
                            _trailersState.value = TrailersUiState.Error(error.message ?: "No hay trailers disponibles por el momento.")
                        }
                    )
                }
            )
        }
    }

    private fun loadStaticLists() {
        viewModelScope.launch {
            repository.getContentConfig().fold(
                onSuccess = { response ->
                    if (!response.countries.isNullOrEmpty()) {
                        countriesList.value = response.countries
                    }
                    if (!response.languages.isNullOrEmpty()) {
                        languagesList.value = response.languages
                    }
                },
                onFailure = {}
            )
            repository.getCountries().fold(
                onSuccess = { response -> 
                    if (countriesList.value.isEmpty()) {
                        countriesList.value = response.countries 
                    }
                },
                onFailure = {}
            )
            repository.getLanguages().fold(
                onSuccess = { response -> 
                    if (languagesList.value.isEmpty()) {
                        languagesList.value = response.languages 
                    }
                },
                onFailure = {}
            )
            repository.getCategories().fold(
                onSuccess = { response -> categoriesList.value = response.categories },
                onFailure = {}
            )
        }
    }

    // --- Ads Operations ---

    private fun loadHomeAds(country: String) {
        viewModelScope.launch {
            repository.getAds(screen = "home", country = country).fold(
                onSuccess = { response ->
                    val activeBanner = response.ads?.firstOrNull { it.type == "banner_horizontal" }
                        ?: response.ads?.firstOrNull { it.type == "banner" }
                    _homeBannerAd.value = activeBanner
                },
                onFailure = {}
            )
        }
    }

    fun trackAdImpression(adId: String, screen: String = "home") {
        viewModelScope.launch {
            repository.trackAdImpression(adId = adId, channelId = null, screen = screen, country = selectedCountry.value)
        }
    }

    fun trackAdClick(adId: String, screen: String = "home") {
        viewModelScope.launch {
            repository.trackAdClick(adId = adId, channelId = null, screen = screen, country = selectedCountry.value)
        }
    }

    fun triggerPlayerAd(channelId: String, onAdReady: (TvAd) -> Unit) {
        if (_appConfig.value?.app?.areAdsEnabled != true) return

        viewModelScope.launch {
            val country = selectedCountry.value
            repository.getAds(screen = "player", country = country, channelId = channelId).fold(
                onSuccess = { response ->
                    val preroll = response.ads?.firstOrNull { it.type == "preroll" && it.is_active != false }
                    if (preroll != null && shouldDisplayAd(preroll)) {
                        _playerPrerollAd.value = preroll
                        onAdReady(preroll)
                        // Update last shown
                        repository.saveLastAdTimestamp(System.currentTimeMillis())
                        sessionImpressionsCount++
                    }
                },
                onFailure = {}
            )
        }
    }

    private fun shouldDisplayAd(ad: TvAd): Boolean {
        // Max impressions per session safety
        if (sessionImpressionsCount >= (ad.max_impressions ?: 10)) return false

        // Frequency checking
        val lastAdTime = repository.lastAdTimestampFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 0L).value
        val diffMs = System.currentTimeMillis() - lastAdTime
        val freqMs = (ad.frequency_minutes ?: 5) * 60 * 1000
        return diffMs >= freqMs
    }

    fun recordAdImpression(adId: String, channelId: String?, screen: String) {
        viewModelScope.launch {
            repository.trackAdImpression(
                adId = adId,
                channelId = channelId,
                screen = screen,
                country = selectedCountry.value
            )
        }
    }

    fun recordAdClick(adId: String, channelId: String?, screen: String) {
        viewModelScope.launch {
            repository.trackAdClick(
                adId = adId,
                channelId = channelId,
                screen = screen,
                country = selectedCountry.value
            )
        }
    }

    // --- Local Operations (Favorites, Watch History, Country selection) ---

    fun changeCountry(countryCode: String) {
        viewModelScope.launch {
            repository.saveCountry(countryCode)
        }
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.saveLanguage(languageCode)
        }
    }

    fun setPlayIntroPreference(playIntro: Boolean) {
        viewModelScope.launch {
            repository.savePlayIntroPref(playIntro)
        }
    }

    fun markIntroAsShown() {
        viewModelScope.launch {
            repository.saveIntroShown(true)
        }
    }

    fun toggleFavorite(channel: TvChannel) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.id == channel.id }
            if (isFav) {
                repository.removeFavorite(channel.id)
            } else {
                repository.addFavorite(channel)
            }
        }
    }

    fun loadChannelDetailsAndPlay(channelId: String) {
        _playerState.value = PlayerUiState.Loading
        viewModelScope.launch {
            repository.getChannel(channelId).fold(
                onSuccess = { response ->
                    val channel = response.channel
                    
                    // Add to local databases reactively
                    repository.addToWatchHistory(channel)

                    // Fetch optional player ad (like banner, overlay or preroll)
                    var playerAd: TvAd? = null
                    if (_appConfig.value?.app?.areAdsEnabled == true) {
                        repository.getAds(screen = "player", country = selectedCountry.value, channelId = channelId).fold(
                            onSuccess = { adsResponse ->
                                playerAd = adsResponse.ads?.firstOrNull { it.is_active != false }
                            },
                            onFailure = {}
                        )
                    }

                    _playerState.value = PlayerUiState.Success(channel, playerAd)
                },
                onFailure = { error ->
                    _playerState.value = PlayerUiState.Error(error.message ?: "Transmisión no disponible por el momento.")
                }
            )
        }
    }

    fun observeIsFavoriteFlow(channelId: String): Flow<Boolean> {
        return repository.observeIsFavorite(channelId)
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            _searchMoviesState.value = MoviesUiState.Success(emptyList())
            return
        }
        _searchMoviesState.value = MoviesUiState.Loading
        viewModelScope.launch {
            repository.searchMovies(query = query, limit = 20).fold(
                onSuccess = { response ->
                    _searchMoviesState.value = MoviesUiState.Success(response.movies ?: emptyList())
                },
                onFailure = { error ->
                    _searchMoviesState.value = MoviesUiState.Error(error.message ?: "No se encontraron películas.")
                }
            )
        }
    }

    // --- Added Functions for Country Selection, Country Content, Series, Novelas & Local Persistence ---

    fun loadContentConfig() {
        viewModelScope.launch {
            repository.getContentConfig().fold(
                onSuccess = { response ->
                    if (!response.countries.isNullOrEmpty()) {
                        countriesList.value = response.countries
                    }
                    if (!response.languages.isNullOrEmpty()) {
                        languagesList.value = response.languages
                    }
                },
                onFailure = {
                    // Fallback to initial country list if API fails
                    countriesList.value = listOf(
                        TvCountry(code = "EC", name = "Ecuador", flag = "🇪🇨"),
                        TvCountry(code = "DO", name = "República Dominicana", flag = "🇩🇴"),
                        TvCountry(code = "HT", name = "Haití", flag = "🇭🇹"),
                        TvCountry(code = "IT", name = "Italia", flag = "🇮🇹")
                    )
                }
            )
        }
    }

    fun loadCountryContent(country: String = selectedCountry.value) {
        if (country.isEmpty()) return
        viewModelScope.launch {
            _countryContentState.value = CountryContentUiState.Loading
            repository.getCountryContent(country).fold(
                onSuccess = { response ->
                    _countryContentState.value = CountryContentUiState.Success(response)
                },
                onFailure = { error ->
                    _countryContentState.value = CountryContentUiState.Error(error.message ?: "No hay datos para mostrar.")
                }
            )
        }
    }

    fun loadShowDetail(showId: String) {
        _showDetailState.value = ShowDetailUiState.Loading
        viewModelScope.launch {
            repository.getShowDetail(showId).fold(
                onSuccess = { detailResponse ->
                    val show = detailResponse.show
                    repository.getShowEpisodes(showId).fold(
                        onSuccess = { episodes ->
                            _showDetailState.value = ShowDetailUiState.Success(show, episodes.episodes ?: emptyList())
                        },
                        onFailure = {
                            _showDetailState.value = ShowDetailUiState.Success(show, emptyList())
                        }
                    )
                },
                onFailure = { error ->
                    _showDetailState.value = ShowDetailUiState.Error(error.message ?: "Error al cargar la serie.")
                }
            )
        }
    }

    fun performCombinedSearch(query: String) {
        if (query.isBlank()) {
            _combinedSearchState.value = CombinedSearchUiState.Idle
            return
        }
        _combinedSearchState.value = CombinedSearchUiState.Loading
        viewModelScope.launch {
            val country = selectedCountry.value.ifEmpty { "EC" }
            repository.countrySearch(country, query).fold(
                onSuccess = { response ->
                    _combinedSearchState.value = CombinedSearchUiState.Success(response)
                },
                onFailure = { error ->
                    _combinedSearchState.value = CombinedSearchUiState.Error(error.message ?: "No se encontraron resultados.")
                }
            )
        }
    }

    fun toggleFavoriteMovie(movie: TvMovie) {
        viewModelScope.launch {
            val isFav = favoriteMovies.value.any { it.id == movie.id }
            if (isFav) {
                repository.removeFavoriteMovie(movie.id)
            } else {
                repository.addFavoriteMovie(movie)
            }
        }
    }

    fun toggleFavoriteShow(show: TvShow) {
        viewModelScope.launch {
            val isFav = favoriteShows.value.any { it.id == show.id }
            if (isFav) {
                repository.removeFavoriteShow(show.id)
            } else {
                repository.addFavoriteShow(show)
            }
        }
    }

    fun addMovieContinueWatching(movie: TvMovie, progress: Float) {
        viewModelScope.launch {
            repository.addContinueWatching(
                id = movie.id,
                title = movie.title,
                poster = movie.displayImageUrl,
                contentType = "movie",
                progress = progress
            )
        }
    }

    fun addShowContinueWatching(show: TvShow, progress: Float) {
        viewModelScope.launch {
            repository.addContinueWatching(
                id = show.id,
                title = show.displayTitle,
                poster = show.displayImageUrl,
                contentType = "show",
                progress = progress
            )
        }
    }

    fun addChannelContinueWatching(channel: TvChannel) {
        viewModelScope.launch {
            repository.addContinueWatching(
                id = channel.id,
                title = channel.name,
                poster = channel.logo,
                contentType = "channel",
                progress = 0f
            )
        }
    }

    fun observeIsFavoriteShowFlow(showId: String): Flow<Boolean> {
        return repository.observeIsFavoriteShow(showId)
    }

    fun observeIsFavoriteMovieFlow(movieId: String): Flow<Boolean> {
        return repository.observeIsFavoriteMovie(movieId)
    }
}
