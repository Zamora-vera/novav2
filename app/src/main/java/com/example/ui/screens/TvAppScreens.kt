package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.api.RetrofitClient
import com.example.data.model.*
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// --- Common UI Components for TV Remote Navigation ---

@Composable
fun TvCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    borderStrokeColor: Color = TvPrimary,
    content: @Composable ColumnScope.(Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1.0f)
    
    val baseModifier = modifier
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .clickable { onClick() }

    val finalModifier = if (testTag != null) baseModifier.testTag(testTag) else baseModifier

    Card(
        modifier = finalModifier,
        shape = RoundedCornerShape(12.dp),
        border = if (isFocused) BorderStroke(2.dp, borderStrokeColor) else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) TvSurfaceFocused else TvSurface,
            contentColor = if (isFocused) TvOnBackground else TvOnSurface
        )
    ) {
        Column {
            content(isFocused)
        }
    }
}

@Composable
fun TvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    containerColor: Color = TvSurface,
    focusedContainerColor: Color = TvPrimary,
    contentColor: Color = TvOnSurface,
    focusedContentColor: Color = Color.Black,
    icon: (@Composable () -> Unit)? = null,
    text: String
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.06f else 1.0f)

    val baseModifier = modifier
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .clickable { onClick() }

    val finalModifier = if (testTag != null) baseModifier.testTag(testTag) else baseModifier

    Surface(
        modifier = finalModifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isFocused) focusedContainerColor else containerColor,
        contentColor = if (isFocused) focusedContentColor else contentColor,
        border = if (isFocused) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TvNavigationMenu(
    navController: NavController,
    currentRoute: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TvBackground.copy(alpha = 0.9f))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left branding
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = TvPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "NOVA TV",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Navigation links
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val menuItems = listOf(
                Pair("home", "Inicio"),
                Pair("channels", "En Vivo"),
                Pair("movies", "Películas"),
                Pair("trailers", "Trailers"),
                Pair("mylist", "Mi Lista"),
                Pair("settings", "Ajustes")
            )

            menuItems.forEach { (route, label) ->
                val isActive = currentRoute == route
                TvButton(
                    onClick = {
                        if (!isActive) {
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    containerColor = if (isActive) TvPrimary.copy(alpha = 0.15f) else Color.Transparent,
                    focusedContainerColor = TvPrimary,
                    contentColor = if (isActive) TvPrimary else TvOnSurface,
                    focusedContentColor = Color.Black,
                    text = label
                )
            }
        }
    }
}

// Helper to construct full media URL
fun fullMediaUrl(path: String?): String {
    return RetrofitClient.fullMediaUrl(path)
}

// --- SCREEN 1: SPLASH SCREEN ---

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val splashState by viewModel.splashState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAppConfig()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground),
        contentAlignment = Alignment.Center
    ) {
        when (val state = splashState) {
            is SplashUiState.Idle, SplashUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = TvPrimary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(color = TvPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando contenido...", color = TvOnSurface, fontSize = 16.sp)
                }
            }
            is SplashUiState.Success -> {
                val config = state.config
                val introVideo = viewModel.introVideo.collectAsState().value
                
                LaunchedEffect(Unit) {
                    val needsCountrySelection = viewModel.selectedCountry.value.isEmpty() || config.app.requiresCountrySelection
                    if (config.app.isIntroEnabled && introVideo != null) {
                        navController.navigate("intro") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else if (needsCountrySelection) {
                        navController.navigate("country_selection") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }
            is SplashUiState.Maintenance -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = TvAccent,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Mantenimiento Activo",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        color = TvOnSurface,
                        fontSize = 18.sp,
                        modifier = Modifier.widthIn(max = 500.dp)
                    )
                }
            }
            is SplashUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TvMutedRed,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TvButton(
                        onClick = { viewModel.loadAppConfig() },
                        text = "Volver a intentar",
                        focusedContainerColor = TvPrimary
                    )
                }
            }
        }
    }
}

// --- SCREEN 2: INTRO SCREEN ---

@Composable
fun IntroScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val context = LocalContext.current
    val introVideo by viewModel.introVideo.collectAsState()
    var showSkipButton by remember { mutableStateOf(false) }
    var skipCounter by remember { mutableStateOf(5) }

    val videoUrl = remember(introVideo) {
        fullMediaUrl(introVideo?.videoUrlResolved)
    }

    val skipAfterSeconds = introVideo?.skipSeconds ?: 5

    // Countdown and skip action
    LaunchedEffect(Unit) {
        for (i in skipAfterSeconds downTo 1) {
            skipCounter = i
            delay(1000)
        }
        showSkipButton = true
        viewModel.markIntroAsShown()
    }

    val onIntroFinished = {
        val needsCountrySelection = viewModel.selectedCountry.value.isEmpty() || viewModel.appConfig.value?.app?.requiresCountrySelection == true
        if (needsCountrySelection) {
            navController.navigate("country_selection") {
                popUpTo("intro") { inclusive = true }
            }
        } else {
            navController.navigate("home") {
                popUpTo("intro") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videoUrl.isNotEmpty()) {
            // Simplified video player using standard Media3 HLS/MP4 integration
            AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        useController = false
                        val player = androidx.media3.exoplayer.ExoPlayer.Builder(ctx).build().apply {
                            setMediaItem(androidx.media3.common.MediaItem.fromUri(videoUrl))
                            prepare()
                            playWhenReady = true
                            addListener(object : androidx.media3.common.Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                                        onIntroFinished()
                                    }
                                }
                            })
                        }
                        this.player = player
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { playerView ->
                    playerView.player?.release()
                }
            )
        } else {
            // Fallback if video url is blank
            LaunchedEffect(Unit) {
                onIntroFinished()
            }
        }

        // Skip floating D-pad button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(48.dp)
        ) {
            if (showSkipButton) {
                TvButton(
                    onClick = onIntroFinished,
                    focusedContainerColor = TvPrimary,
                    text = "Saltar Intro"
                )
            } else {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Saltar en $skipCounter...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- SCREEN 3: HOME SCREEN ---

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val homeState by viewModel.homeState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val mostWatched by viewModel.mostWatched.collectAsState()
    val activeBannerAd by viewModel.homeBannerAd.collectAsState()
    val countryContentState by viewModel.countryContentState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "home")

        when (val state = homeState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TvPrimary)
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = TvOnSurface, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        TvButton(
                            onClick = { viewModel.loadHomeContent() },
                            text = "Volver a intentar",
                            focusedContainerColor = TvPrimary
                        )
                    }
                }
            }
            is HomeUiState.Success -> {
                val data = state.homeData
                var selectedFilter by remember { mutableStateOf("All") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Horizontal Filter Bar at the top of the content
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .testTag("home_filter_bar"),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filterOptions = listOf(
                            "All" to "Todo",
                            "Live TV" to "En Vivo",
                            "Movies" to "Películas",
                            "Trending" to "Tendencias",
                            "Trailers" to "Trailers"
                        )

                        filterOptions.forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            var isFocused by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(if (isFocused) 1.08f else 1.0f)

                            Surface(
                                modifier = Modifier
                                    .scale(scale)
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .clickable { selectedFilter = key }
                                    .testTag("filter_chip_$key"),
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    isFocused -> TvPrimary
                                    isSelected -> TvPrimary.copy(alpha = 0.25f)
                                    else -> TvSurface
                                },
                                contentColor = when {
                                    isFocused -> Color.Black
                                    isSelected -> TvPrimary
                                    else -> TvOnSurface
                                },
                                border = BorderStroke(
                                    width = if (isFocused) 0.dp else 1.dp,
                                    color = if (isSelected) TvPrimary else Color.White.copy(alpha = 0.15f)
                                )
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        // HERO HEADER
                        val featured = data.featured_channels?.firstOrNull()
                        if (featured != null && (selectedFilter == "All" || selectedFilter == "Live TV" || selectedFilter == "Trending")) {
                            item {
                                HomeHeroHeader(featured = featured) {
                                    navController.navigate("player/${featured.id}")
                                }
                            }
                        }

                        // FILA 1: Continuar viendo (Watch History)
                        if (watchHistory.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Live TV" || selectedFilter == "Movies")) {
                            item {
                                HomeRowHeader(title = "Continuar viendo")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(watchHistory) { item ->
                                        ChannelCard(
                                            id = item.id,
                                            name = item.name,
                                            logoUrl = item.logo,
                                            onSelected = { navController.navigate("player/${item.id}") }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 2: Canales destacados
                        val featuredChannels = data.featured_channels ?: emptyList()
                        if (featuredChannels.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Live TV" || selectedFilter == "Trending")) {
                            item {
                                HomeRowHeader(title = "Canales Destacados")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(featuredChannels) { channel ->
                                        ChannelCard(
                                            id = channel.id,
                                            name = channel.name,
                                            logoUrl = channel.logo,
                                            category = channel.category,
                                            onSelected = { navController.navigate("player/${channel.id}") }
                                        )
                                    }
                                }
                            }
                        }

                        // NUEVA SECCIÓN DILIGENTE: Contenido del país seleccionado (Películas, Series, Novelas, Trailers de Nova TV)
                        when (val ccState = countryContentState) {
                            is TvViewModel.CountryContentUiState.Success -> {
                                val cc = ccState.data
                                
                                // Fila: Series de Estreno
                                val countrySeries: List<TvShow> = cc.series ?: cc.shows ?: emptyList<TvShow>()
                                if (countrySeries.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Trending")) {
                                    item {
                                        HomeRowHeader(title = "Series de Estreno en tu País")
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            items(countrySeries) { show: TvShow ->
                                                TvShowCard(show = show) {
                                                    navController.navigate("show_detail/${show.id}")
                                                }
                                            }
                                        }
                                    }
                                }

                                // Fila: Novelas Nova TV
                                val countryNovelas: List<TvShow> = cc.novelas ?: emptyList<TvShow>()
                                if (countryNovelas.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Trending")) {
                                    item {
                                        HomeRowHeader(title = "Novelas de Éxito")
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            items(countryNovelas) { novela: TvShow ->
                                                TvShowCard(show = novela) {
                                                    navController.navigate("show_detail/${novela.id}")
                                                }
                                            }
                                        }
                                    }
                                }

                                // Fila: Películas recomendadas por País
                                val countryMovies: List<TvMovie> = cc.movies ?: cc.free_content ?: emptyList<TvMovie>()
                                if (countryMovies.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Movies")) {
                                    item {
                                        HomeRowHeader(title = "Películas Sugeridas para ti")
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            items(countryMovies) { movie: TvMovie ->
                                                MovieCard(
                                                    movie = movie,
                                                    onSelected = { navController.navigate("movie_detail/${movie.id}") }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Fila: Trailers sugeridos
                                val countryTrailers: List<TvTrailer> = cc.trailers ?: emptyList<TvTrailer>()
                                if (countryTrailers.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Trailers")) {
                                    item {
                                        HomeRowHeader(title = "Trailers y Avances Exclusivos")
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            items(countryTrailers) { trailer: TvTrailer ->
                                                TrailerCard(
                                                    trailer = trailer,
                                                    onSelected = {
                                                        launchYoutube(navController.context, trailer.youtube_video_id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            else -> { /* no-op or loading, standard fallback is fine */ }
                        }

                        // BANNER PUBLICITARIO (Home Banner Ad)
                        if (activeBannerAd != null) {
                            item {
                                HomeBannerAd(ad = activeBannerAd!!, viewModel = viewModel)
                            }
                        }

                        // FILA 3: Más vistos por ti (calculated locally)
                        if (mostWatched.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Trending")) {
                            item {
                                HomeRowHeader(title = "Más Vistos por Ti")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(mostWatched) { item ->
                                        ChannelCard(
                                            id = item.id,
                                            name = item.title,
                                            logoUrl = item.logo,
                                            onSelected = { navController.navigate("player/${item.id}") }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 4: Mis canales (Favorites)
                        if (favorites.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Live TV")) {
                            item {
                                HomeRowHeader(title = "Mis Canales Favoritos")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(favorites) { fav ->
                                        ChannelCard(
                                            id = fav.id,
                                            name = fav.name,
                                            logoUrl = fav.logo,
                                            onSelected = { navController.navigate("player/${fav.id}") }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 5: Categorías
                        val categories = data.categories ?: emptyList()
                        if (categories.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Live TV")) {
                            item {
                                HomeRowHeader(title = "Categorías de Canales")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(categories) { category ->
                                        CategoryCard(
                                            category = category,
                                            onSelected = {
                                                navController.navigate("channels?category=${category.name}")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 6: Países
                        val countries = data.countries ?: emptyList()
                        if (countries.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Live TV")) {
                            item {
                                HomeRowHeader(title = "Países")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(countries) { country ->
                                        CountryCard(
                                            country = country,
                                            onSelected = {
                                                viewModel.changeCountry(country.code)
                                                Toast.makeText(navController.context, "País cambiado a: ${country.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 7: Películas
                        val movies = data.movies ?: emptyList()
                        if (movies.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Movies")) {
                            item {
                                HomeRowHeader(title = "Películas Gratis")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(movies) { movie ->
                                        MovieCard(
                                            movie = movie,
                                            onSelected = { navController.navigate("movie_detail/${movie.id}") }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 8: Trailers
                        val trailers = data.trailers ?: emptyList()
                        if (trailers.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Trailers")) {
                            item {
                                HomeRowHeader(title = "Nuevos Trailers")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(trailers) { trailer ->
                                        TrailerCard(
                                            trailer = trailer,
                                            onSelected = {
                                                launchYoutube(navController.context, trailer.youtube_video_id)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // FILA 9: Anuncio patrocinado (Banner Ad)
                        if (activeBannerAd != null) {
                            item {
                                HomeRowHeader(title = "Contenido Patrocinado")
                                Box(modifier = Modifier.padding(vertical = 8.dp)) {
                                    SponsoredBannerCard(
                                        ad = activeBannerAd!!,
                                        onImpression = {
                                            viewModel.recordAdImpression(activeBannerAd!!.id, null, "home")
                                        },
                                        onSelected = {
                                            viewModel.recordAdClick(activeBannerAd!!.id, null, "home")
                                            if (!activeBannerAd!!.click_url.isNullOrBlank()) {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeBannerAd!!.click_url))
                                                    navController.context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(navController.context, "Abriendo enlace...", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: LIVE CHANNELS GRID SCREEN ---

@Composable
fun LiveChannelsScreen(
    navController: NavController,
    viewModel: TvViewModel,
    initialCategory: String? = null
) {
    val channelsState by viewModel.channelsState.collectAsState()
    val categories = viewModel.categoriesList.collectAsState().value
    var selectedCategoryFilter by remember { mutableStateOf(initialCategory ?: "Todos") }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "channels")

        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category horizontal pill row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    TvButton(
                        onClick = { selectedCategoryFilter = "Todos" },
                        containerColor = if (selectedCategoryFilter == "Todos") TvPrimary.copy(alpha = 0.2f) else TvSurface,
                        focusedContainerColor = TvPrimary,
                        contentColor = if (selectedCategoryFilter == "Todos") TvPrimary else TvOnSurface,
                        text = "Todos"
                    )
                }

                items(categories) { category ->
                    val isSelected = selectedCategoryFilter == category.name
                    TvButton(
                        onClick = { selectedCategoryFilter = category.name },
                        containerColor = if (isSelected) TvPrimary.copy(alpha = 0.2f) else TvSurface,
                        focusedContainerColor = TvPrimary,
                        contentColor = if (isSelected) TvPrimary else TvOnSurface,
                        text = category.name
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Minimal search icon
            TvButton(
                onClick = { navController.navigate("search") },
                focusedContainerColor = TvPrimary,
                icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = "Buscar"
            )
        }

        // Live Grid
        when (val state = channelsState) {
            is ChannelsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TvPrimary)
                }
            }
            is ChannelsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = TvOnSurface, fontSize = 16.sp)
                }
            }
            is ChannelsUiState.Success -> {
                val filteredList = state.channels.filter { channel ->
                    val matchesCategory = selectedCategoryFilter == "Todos" || channel.category == selectedCategoryFilter
                    val matchesSearch = channel.name.contains(searchQuery, ignoreCase = true)
                    matchesCategory && matchesSearch
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay canales disponibles por el momento.", color = TvOnSurface, fontSize = 16.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList) { channel ->
                            ChannelCard(
                                id = channel.id,
                                name = channel.name,
                                logoUrl = channel.logo,
                                category = channel.category,
                                onSelected = { navController.navigate("player/${channel.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: CHANNEL PLAYER SCREEN (ExoPlayer HLS & YouTube Embed Fallback) ---

@Composable
fun ChannelPlayerScreen(
    navController: NavController,
    viewModel: TvViewModel,
    channelId: String
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val isFavoriteFlow = remember(channelId) { viewModel.observeIsFavoriteFlow(channelId) }
    val isFavorite by isFavoriteFlow.collectAsState(initial = false)

    var showControls by remember { mutableStateOf(true) }
    var activeAd by remember { mutableStateOf<TvAd?>(null) }
    var skipTimer by remember { mutableStateOf(5) }
    var showAdSkipBtn by remember { mutableStateOf(false) }

    // Fetch channel details on launch
    LaunchedEffect(channelId) {
        viewModel.loadChannelDetailsAndPlay(channelId)
    }

    // Auto-hide playback overlay controls after 6 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(6000)
            showControls = false
        }
    }

    // Countdown if ad is active
    LaunchedEffect(activeAd) {
        if (activeAd != null) {
            viewModel.recordAdImpression(activeAd!!.id, channelId, "player")
            val skipSecs = activeAd!!.skip_after_seconds ?: 5
            for (i in skipSecs downTo 1) {
                skipTimer = i
                delay(1000)
            }
            showAdSkipBtn = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = true }
    ) {
        when (val state = playerState) {
            is PlayerUiState.Idle, PlayerUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TvPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando transmisión...", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
            is PlayerUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = TvMutedRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        TvButton(
                            onClick = { navController.popBackStack() },
                            text = "Volver",
                            focusedContainerColor = TvPrimary
                        )
                    }
                }
            }
            is PlayerUiState.Success -> {
                val channel = state.channel
                val streamUrl = remember(channel) { fullMediaUrl(channel.resolvedStreamUrl) }

                // Check pre-roll ads on initial playback success
                LaunchedEffect(Unit) {
                    viewModel.triggerPlayerAd(channel.id) { ad ->
                        activeAd = ad
                    }
                }

                if (activeAd != null) {
                    // AD SCREEN (PRE-ROLL)
                    val adVideoUrl = remember(activeAd) { fullMediaUrl(activeAd!!.media_url) }
                    
                    if (adVideoUrl.isNotEmpty() && adVideoUrl.endsWith(".mp4")) {
                        AndroidView(
                            factory = { ctx ->
                                androidx.media3.ui.PlayerView(ctx).apply {
                                    useController = false
                                    val player = androidx.media3.exoplayer.ExoPlayer.Builder(ctx).build().apply {
                                        setMediaItem(androidx.media3.common.MediaItem.fromUri(adVideoUrl))
                                        prepare()
                                        playWhenReady = true
                                        addListener(object : androidx.media3.common.Player.Listener {
                                            override fun onPlaybackStateChanged(playbackState: Int) {
                                                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                                                    activeAd = null
                                                }
                                            }
                                        })
                                    }
                                    this.player = player
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            onRelease = { playerView ->
                                playerView.player?.release()
                            }
                        )
                    } else {
                        // Static Image Ad
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = adVideoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        LaunchedEffect(Unit) {
                            delay(8000)
                            activeAd = null
                        }
                    }

                    // Skipper banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(48.dp)
                    ) {
                        if (showAdSkipBtn) {
                            TvButton(
                                onClick = { activeAd = null },
                                focusedContainerColor = TvPrimary,
                                text = "Saltar Anuncio"
                            )
                        } else {
                            Surface(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Publicidad - Saltar en $skipTimer...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // MAIN CONTENT STREAM SCREEN
                    if (channel.resolvedPlaybackType == "youtube_embed" || !channel.youtube_video_id.isNullOrBlank()) {
                        // YouTube embed fallback
                        val yid = channel.youtube_video_id ?: "7_96O4gU8-w"
                        val contextForIntent = LocalContext.current
                        
                        var launchFailed by remember { mutableStateOf(false) }

                        LaunchedEffect(yid) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$yid")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            if (intent.resolveActivity(contextForIntent.packageManager) != null) {
                                contextForIntent.startActivity(intent)
                                navController.popBackStack()
                            } else {
                                launchFailed = true
                            }
                        }

                        if (launchFailed) {
                            YouTubeWebViewPlayer(videoId = yid)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TvPrimary)
                            }
                        }
                    } else {
                        // Standard ExoPlayer HLS/M3U8 Player
                        AndroidView(
                            factory = { ctx ->
                                androidx.media3.ui.PlayerView(ctx).apply {
                                     useController = true
                                     val player = androidx.media3.exoplayer.ExoPlayer.Builder(ctx).build().apply {
                                         setMediaItem(androidx.media3.common.MediaItem.fromUri(streamUrl))
                                        prepare()
                                        playWhenReady = true
                                    }
                                    this.player = player
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            onRelease = { playerView ->
                                playerView.player?.release()
                            }
                        )
                    }

                    // OVERLAY HUD CONTROLS
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.8f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        ) {
                            // Top HUD
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .align(Alignment.TopStart),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TvButton(
                                    onClick = { navController.popBackStack() },
                                    focusedContainerColor = TvPrimary,
                                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text = "Volver"
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                AsyncImage(
                                    model = fullMediaUrl(channel.logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = channel.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = channel.category ?: "Televisión en vivo",
                                        color = TvPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Live badge
                                Surface(
                                    color = TvMutedRed,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "EN VIVO",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Toggle Favorite D-Pad trigger
                                TvButton(
                                    onClick = { viewModel.toggleFavorite(channel) },
                                    containerColor = if (isFavorite) TvPrimary.copy(alpha = 0.2f) else TvSurface,
                                    focusedContainerColor = TvPrimary,
                                    contentColor = if (isFavorite) TvPrimary else Color.White,
                                    icon = {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (isFavorite) TvPrimary else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = if (isFavorite) "Quitar de Mi Lista" else "Añadir a Mi Lista"
                                )
                            }

                            // Bottom optional metadata info panel
                            if (!channel.description.isNullOrBlank()) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(24.dp)
                                        .widthIn(max = 600.dp)
                                ) {
                                    Text(
                                        text = "Sobre este canal",
                                        color = TvPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = channel.description,
                                        color = TvOnSurface,
                                        fontSize = 13.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom WebView player for YouTube fallback (essential for Fire TV / TVs without GMS)
@Composable
fun YouTubeWebViewPlayer(videoId: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                loadUrl("https://www.youtube.com/embed/$videoId?autoplay=1&fs=1")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// --- SCREEN 6: FAVORITE MY LIST SCREEN ---

@Composable
fun MyListScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val favorites by viewModel.favorites.collectAsState()
    val favoriteMovies by viewModel.favoriteMovies.collectAsState()
    val favoriteShows by viewModel.favoriteShows.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mis Canales", "Mis Películas", "Mis Series", "Continuar Viendo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "mylist")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Multi-tab list selector row (focusable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    var isFocused by remember { mutableStateOf(false) }
                    Surface(
                        color = if (selectedTab == index) TvPrimary else if (isFocused) Color(0xFF2C2C3C) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusable()
                            .clickable { selectedTab = index }
                            .border(
                                width = 1.dp,
                                color = if (isFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display content based on active tab
            when (selectedTab) {
                0 -> {
                    // Mis Canales
                    if (favorites.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No has agregado ningún canal a tus favoritos.", color = TvOnSurface, fontSize = 15.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(160.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(favorites) { item ->
                                ChannelCard(
                                    id = item.id,
                                    name = item.name,
                                    logoUrl = item.logo,
                                    onSelected = { navController.navigate("player/${item.id}") }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Mis Películas
                    if (favoriteMovies.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No has agregado ninguna película a tus favoritos.", color = TvOnSurface, fontSize = 15.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(140.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(favoriteMovies) { item ->
                                val movie = TvMovie(
                                    id = item.id,
                                    title = item.title,
                                    description = item.description,
                                    image_url = item.poster,
                                    stream_url = null
                                )
                                MovieCard(
                                    movie = movie,
                                    onSelected = { navController.navigate("movie_detail/${item.id}") }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Mis Series
                    if (favoriteShows.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No has agregado ninguna serie a tus favoritos.", color = TvOnSurface, fontSize = 15.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(140.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(favoriteShows) { item ->
                                var isFocusedCard by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(210.dp)
                                        .onFocusChanged { isFocusedCard = it.isFocused }
                                        .focusable()
                                        .clickable { navController.navigate("show_detail/${item.id}") }
                                        .border(
                                            width = 2.dp,
                                            color = if (isFocusedCard) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        val posterUrl = fullMediaUrl(item.poster)
                                        if (posterUrl.isNotEmpty()) {
                                            coil.compose.AsyncImage(
                                                model = posterUrl,
                                                contentDescription = item.name,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFF23232F)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(item.name, color = Color.White, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Continuar Viendo
                    if (continueWatching.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No tienes contenido pendiente para continuar viendo.", color = TvOnSurface, fontSize = 15.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(160.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(continueWatching) { item ->
                                var isFocusedCard by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .onFocusChanged { isFocusedCard = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            if (item.contentType == "movie") {
                                                navController.navigate("movie_detail/${item.id}")
                                            } else if (item.contentType == "show") {
                                                navController.navigate("show_detail/${item.id}")
                                            } else {
                                                navController.navigate("player/${item.id}")
                                            }
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (isFocusedCard) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF23232F))
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            val posterUrl = fullMediaUrl(item.poster)
                                            if (posterUrl.isNotEmpty()) {
                                                coil.compose.AsyncImage(
                                                    model = posterUrl,
                                                    contentDescription = item.title,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFF2C2C3D)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(item.title, color = Color.White, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(item.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = item.progress,
                                                color = TvPrimary,
                                                trackColor = Color.DarkGray,
                                                modifier = Modifier.fillMaxWidth().height(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: MOVIES CATALOG SCREEN ---

@Composable
fun MoviesScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val moviesState by viewModel.moviesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "movies")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Películas Gratis Disponibles",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = moviesState) {
                is MoviesUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TvPrimary)
                    }
                }
                is MoviesUiState.Error -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = TvOnSurface, fontSize = 15.sp)
                    }
                }
                is MoviesUiState.Success -> {
                    if (state.movies.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay películas disponibles por el momento.", color = TvOnSurface, fontSize = 15.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(140.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.movies) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onSelected = { navController.navigate("movie_detail/${movie.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 8: MOVIE DETAIL SHEET DIALOG SCREEN ---

@Composable
fun MovieDetailScreen(
    navController: NavController,
    viewModel: TvViewModel,
    movieId: String
) {
    val moviesState by viewModel.moviesState.collectAsState()
    val searchMoviesState by viewModel.searchMoviesState.collectAsState()
    val context = LocalContext.current

    val movie = remember(moviesState, searchMoviesState) {
        val list = mutableListOf<TvMovie>()
        if (moviesState is MoviesUiState.Success) {
            list.addAll((moviesState as MoviesUiState.Success).movies)
        }
        if (searchMoviesState is MoviesUiState.Success) {
            list.addAll((searchMoviesState as MoviesUiState.Success).movies)
        }
        list.find { it.id == movieId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        if (movie == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay datos para mostrar.", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    TvButton(onClick = { navController.popBackStack() }, text = "Volver")
                }
            }
        } else {
            // Hero background blur
            AsyncImage(
                model = fullMediaUrl(movie.displayImageUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().scale(1.2f),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )

            // Content Column
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Poster
                AsyncImage(
                    model = fullMediaUrl(movie.displayImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(36.dp))

                // Metadata Details
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = TvPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = movie.displayGenres.joinToString(", ") ?: "Cine",
                                color = TvPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = movie.year?.toString() ?: "2023",
                            color = TvOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Star rating
                        Row {
                            repeat((movie.displayRating ?: 5.0).toInt()) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = TvAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = movie.description ?: "No hay descripción disponible para esta película por el momento.",
                        color = TvOnSurface,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row {
                        // Reproducir Película (only if stream_url is present and playback_type is not metadata_only)
                        val isMetadataOnly = movie.playback_type == "metadata_only"
                        if (!movie.stream_url.isNullOrBlank() && !isMetadataOnly) {
                            TvButton(
                                onClick = {
                                    val stream = fullMediaUrl(movie.stream_url)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stream))
                                    context.startActivity(intent)
                                },
                                focusedContainerColor = TvPrimary,
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black) },
                                text = "Reproducir Película"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        // Ver en plataforma oficial (if watch_url exists)
                        if (!movie.watch_url.isNullOrBlank()) {
                            TvButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(movie.watch_url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo abrir la plataforma oficial", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                focusedContainerColor = TvPrimary,
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black) },
                                text = "Ver en plataforma oficial"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        // Ver Trailer (if youtube_video_id exists or has_trailer is true)
                        val youtubeId = movie.displayYoutubeId
                        if (!youtubeId.isNullOrBlank() || movie.has_trailer == true) {
                            TvButton(
                                onClick = {
                                    val yid = youtubeId ?: "7_96O4gU8-w"
                                    launchYoutube(context, yid)
                                },
                                focusedContainerColor = TvSecondary,
                                icon = { Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White) },
                                text = "Ver Trailer"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        TvButton(
                            onClick = { navController.popBackStack() },
                            containerColor = Color.Transparent,
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            text = "Volver"
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 9: TRAILERS SCREEN ---

@Composable
fun TrailersScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val trailersState by viewModel.trailersState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "trailers")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trailers Cinematográficos Recomendados",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = trailersState) {
                is TrailersUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TvPrimary)
                    }
                }
                is TrailersUiState.Error -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = TvOnSurface, fontSize = 15.sp)
                    }
                }
                is TrailersUiState.Success -> {
                    if (state.trailers.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay trailers disponibles.", color = TvOnSurface, fontSize = 15.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(180.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.trailers) { trailer ->
                                TrailerCard(
                                    trailer = trailer,
                                    onSelected = {
                                        launchYoutube(context, trailer.youtube_video_id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 10: SEARCH SEARCH SCREEN ---

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val combinedSearchState by viewModel.combinedSearchState.collectAsState()

    // Trigger debounced combined search on query change
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            delay(400)
            viewModel.performCombinedSearch(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "search")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // TV Friendly Search Row (D-pad input simulated / clear button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Escriba para buscar canales, películas, series o novelas...", color = TvTextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .background(TvSurface, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TvSurfaceFocused,
                        unfocusedContainerColor = TvSurface,
                        disabledContainerColor = TvSurface,
                        focusedIndicatorColor = TvPrimary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                if (query.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    TvButton(
                        onClick = { query = "" },
                        text = "Limpiar"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search results parsing
            if (query.isBlank()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TvTextMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Busque canales, películas, series y novelas.", color = TvOnSurface)
                    }
                }
            } else {
                when (val state = combinedSearchState) {
                    is TvViewModel.CombinedSearchUiState.Idle -> {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Escriba para comenzar a buscar...", color = TvOnSurface)
                        }
                    }
                    is TvViewModel.CombinedSearchUiState.Loading -> {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TvPrimary)
                        }
                    }
                    is TvViewModel.CombinedSearchUiState.Error -> {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = TvOnSurface, fontSize = 15.sp)
                        }
                    }
                    is TvViewModel.CombinedSearchUiState.Success -> {
                        val channels: List<TvChannel> = state.result.channels ?: emptyList<TvChannel>()
                        val movies: List<TvMovie> = state.result.movies ?: emptyList<TvMovie>()
                        val shows: List<TvShow> = state.result.shows ?: emptyList<TvShow>()

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            // Canales
                            if (channels.isNotEmpty()) {
                                item {
                                    Text("Canales Encontrados (${channels.size})", color = TvPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(channels) { channel ->
                                            ChannelCard(
                                                id = channel.id,
                                                name = channel.name,
                                                logoUrl = channel.logo,
                                                onSelected = { navController.navigate("player/${channel.id}") }
                                            )
                                        }
                                    }
                                }
                            }

                            // Películas
                            if (movies.isNotEmpty()) {
                                item {
                                    Text("Películas Encontradas (${movies.size})", color = TvPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(movies) { movie ->
                                            MovieCard(
                                                movie = movie,
                                                onSelected = { navController.navigate("movie_detail/${movie.id}") }
                                            )
                                        }
                                    }
                                }
                            }

                            // Series/Shows
                            if (shows.isNotEmpty()) {
                                item {
                                    Text("Series y Novelas Encontradas (${shows.size})", color = TvPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(shows) { show ->
                                            TvShowCard(show = show) {
                                                navController.navigate("show_detail/${show.id}")
                                            }
                                        }
                                    }
                                }
                            }

                            // No results state
                            if (channels.isEmpty() && movies.isEmpty() && shows.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxHeight(0.6f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No hay datos para mostrar con esa búsqueda.", color = TvOnSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 11: SETTINGS / AJUSTES SCREEN ---

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val context = LocalContext.current
    val currentCountry by viewModel.selectedCountry.collectAsState()
    val currentLanguage by viewModel.selectedLanguage.collectAsState()
    val playIntro by viewModel.playIntroPref.collectAsState()

    val countries = viewModel.countriesList.collectAsState().value
    val languages = viewModel.languagesList.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        TvNavigationMenu(navController = navController, currentRoute = "settings")

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Left menu lists for choices
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Configuración de Nova TV", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Intro setting
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TvSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reproducir Intro de Bienvenida", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Cargar video de bienvenida al iniciar la aplicación", color = TvOnSurface, fontSize = 12.sp)
                            }
                            Switch(
                                checked = playIntro,
                                onCheckedChange = { viewModel.setPlayIntroPreference(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = TvPrimary)
                            )
                        }
                    }
                }

                // Country Picker
                item {
                    Text("País Seleccionado (${currentCountry})", color = TvPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val staticCountries = countries.ifEmpty {
                            listOf(
                                TvCountry("DO", "República Dominicana"),
                                TvCountry("US", "Estados Unidos"),
                                TvCountry("ES", "España"),
                                TvCountry("MX", "México")
                            )
                        }
                        items(staticCountries) { country ->
                            val isSelected = currentCountry == country.code
                            TvButton(
                                onClick = {
                                    viewModel.changeCountry(country.code)
                                    Toast.makeText(context, "País cambiado a: ${country.name}", Toast.LENGTH_SHORT).show()
                                },
                                containerColor = if (isSelected) TvPrimary.copy(alpha = 0.2f) else TvSurface,
                                focusedContainerColor = TvPrimary,
                                contentColor = if (isSelected) TvPrimary else Color.White,
                                text = country.name
                            )
                        }
                    }
                }

                // Language Picker
                item {
                    Text("Idioma Seleccionado (${currentLanguage})", color = TvPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val staticLanguages = languages.ifEmpty {
                            listOf(
                                TvLanguage("es", "Español"),
                                TvLanguage("en", "Inglés"),
                                TvLanguage("de", "Alemán")
                            )
                        }
                        items(staticLanguages) { lang ->
                            val isSelected = currentLanguage == lang.code
                            TvButton(
                                onClick = {
                                    viewModel.changeLanguage(lang.code)
                                    Toast.makeText(context, "Idioma cambiado a: ${lang.name}", Toast.LENGTH_SHORT).show()
                                },
                                containerColor = if (isSelected) TvPrimary.copy(alpha = 0.2f) else TvSurface,
                                focusedContainerColor = TvPrimary,
                                contentColor = if (isSelected) TvPrimary else Color.White,
                                text = lang.name
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Right sidebar: Session metadata
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = TvSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Información de Sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    Divider(color = Color.White.copy(alpha = 0.12f))

                    Column {
                        Text("Soporte Fire TV", color = TvPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("La navegación D-pad está 100% optimizada para el control de su Fire TV u Android TV.", color = TvOnSurface, fontSize = 12.sp)
                    }

                    Column {
                        Text("Suscripción", color = TvPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Gratuito. Financiado con anuncios comerciales remotos.", color = TvOnSurface, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column {
                        Text("Nova TV v1.0.0", color = TvTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Sin registro, sin login.", color = TvTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS FOR HOME ROW SECTIONS ---

@Composable
fun HomeHeroHeader(
    featured: TvChannel,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TvSurface)
    ) {
        // Overlay blur background
        AsyncImage(
            model = fullMediaUrl(featured.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        // Shade gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)
                    )
                )
        )

        // Contents
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(28.dp)
                .widthIn(max = 450.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = TvMutedRed,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "RECOMENDADO",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = featured.name,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = featured.description ?: "Transmisión premium sin cortes 24/7. Nova TV te ofrece entretenimiento en vivo gratis en tu televisor.",
                color = TvOnSurface,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            TvButton(
                onClick = onPlay,
                focusedContainerColor = TvPrimary,
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black) },
                text = "Reproducir Ahora"
            )
        }
    }
}

@Composable
fun HomeRowHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun ChannelCard(
    id: String,
    name: String,
    logoUrl: String?,
    category: String? = null,
    onSelected: () -> Unit
) {
    TvCard(
        onClick = onSelected,
        modifier = Modifier.width(150.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(TvBackground)
        ) {
            AsyncImage(
                model = fullMediaUrl(logoUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = name,
                color = if (isFocused) Color.White else TvOnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (category != null) {
                Text(
                    text = category,
                    color = TvTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HomeBannerAd(
    ad: TvAd,
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var hasTrackedImpression by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.03f else 1.0f)
    val context = androidx.compose.ui.platform.LocalContext.current

    // Track impression once per banner appearance
    LaunchedEffect(ad.id) {
        if (!hasTrackedImpression) {
            viewModel.trackAdImpression(ad.id)
            hasTrackedImpression = true
        }
    }

    // Trigger impression when focused too as fallback
    LaunchedEffect(isFocused) {
        if (isFocused && !hasTrackedImpression) {
            viewModel.trackAdImpression(ad.id)
            hasTrackedImpression = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable {
                viewModel.trackAdClick(ad.id)
                if (!ad.targetUrl.isNullOrBlank()) {
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(ad.targetUrl)
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            context,
                            "No se pudo abrir el enlace",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .testTag("home_banner_ad"),
        shape = RoundedCornerShape(12.dp),
        border = if (isFocused) BorderStroke(3.dp, TvPrimary) else BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(
            containerColor = TvSurface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ad Banner Image
            AsyncImage(
                model = fullMediaUrl(ad.imageUrl),
                contentDescription = ad.name ?: "Publicidad",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic Dark Gradient Overlay to make text readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            startX = 0.0f,
                            endX = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Content Overlay
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Badge / Client name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(TvPrimary.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .border(1.dp, TvPrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = ad.client ?: "Anuncio",
                                color = TvPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = ad.name ?: "Publicidad",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Description text
                    if (!ad.shortText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ad.shortText,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // CTA Button
                val ctaText = ad.buttonText ?: "Más información"
                Box(
                    modifier = Modifier
                        .background(
                            if (isFocused) Color.White else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = ctaText,
                        color = if (isFocused) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: TvCategory,
    onSelected: () -> Unit
) {
    TvCard(
        onClick = onSelected,
        modifier = Modifier.width(140.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(TvSecondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = if (isFocused) TvPrimary else TvOnSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = category.name,
            color = if (isFocused) Color.White else TvOnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CountryCard(
    country: TvCountry,
    onSelected: () -> Unit
) {
    TvCard(
        onClick = onSelected,
        modifier = Modifier.width(140.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(TvSurfaceFocused),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.code,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isFocused) TvPrimary else TvOnSurface
            )
        }
        Text(
            text = country.name,
            color = if (isFocused) Color.White else TvOnSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MovieCard(
    movie: TvMovie,
    onSelected: () -> Unit
) {
    TvCard(
        onClick = onSelected,
        modifier = Modifier.width(120.dp)
    ) { isFocused ->
        AsyncImage(
            model = fullMediaUrl(movie.displayImageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = movie.title,
            color = if (isFocused) Color.White else TvOnSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TrailerCard(
    trailer: TvTrailer,
    onSelected: () -> Unit
) {
    TvCard(
        onClick = onSelected,
        modifier = Modifier.width(180.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            AsyncImage(
                model = fullMediaUrl(trailer.thumbnail),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
            )
        }
        Text(
            text = trailer.title,
            color = if (isFocused) Color.White else TvOnSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SponsoredBannerCard(
    ad: TvAd,
    onImpression: () -> Unit,
    onSelected: () -> Unit
) {
    LaunchedEffect(ad.id) {
        onImpression()
    }

    TvCard(
        onClick = onSelected,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        borderStrokeColor = TvAccent
    ) { isFocused ->
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fullMediaUrl(ad.media_url),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = TvAccent,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "PATROCINADO",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ad.title ?: "Anuncio Patrocinado de Nova TV",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            TvButton(
                onClick = onSelected,
                focusedContainerColor = TvAccent,
                text = "Saber más",
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

// Global Youtube Intent helper launcher
fun launchYoutube(context: Context, videoId: String?) {
    if (videoId.isNullOrBlank()) return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web link
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(context, "No se pudo abrir YouTube.", Toast.LENGTH_SHORT).show()
        }
    }
}

// --- NEW COMPOSABLES: CountrySelectionScreen, ShowDetailScreen, and TvShowCard ---

@Composable
fun CountrySelectionScreen(
    navController: NavController,
    viewModel: TvViewModel
) {
    val countries by viewModel.countriesList.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1E2E), Color(0xFF0F0F14)),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = appConfig?.app?.name ?: "Nova TV",
                color = TvPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecciona tu país para personalizar tu experiencia",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Select your country to customize your experience",
                color = TvTextMuted,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                countries.forEach { country ->
                    var isFocused by remember { mutableStateOf(false) }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFocused) TvPrimary else Color(0xFF23232F)
                        ),
                        modifier = Modifier
                            .width(180.dp)
                            .height(130.dp)
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                viewModel.changeCountry(country.code)
                                val defaultLang = when (country.code) {
                                    "EC", "DO" -> "es"
                                    "HT" -> "fr"
                                    "IT" -> "it"
                                    else -> "es"
                                }
                                viewModel.changeLanguage(defaultLang)
                                navController.navigate("home") {
                                    popUpTo("country_selection") { inclusive = true }
                                }
                            }
                            .border(
                                width = 2.dp,
                                color = if (isFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = country.flag ?: "🌐",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = country.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShowDetailScreen(
    navController: NavController,
    viewModel: TvViewModel,
    showId: String
) {
    val showDetailState by viewModel.showDetailState.collectAsState()
    val favorites by viewModel.favoriteShows.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(showId) {
        viewModel.loadShowDetail(showId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
    ) {
        when (val state = showDetailState) {
            is TvViewModel.ShowDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TvPrimary)
                }
            }
            is TvViewModel.ShowDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = TvOnSurface, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        TvButton(
                            onClick = { viewModel.loadShowDetail(showId) },
                            text = "Reintentar",
                            focusedContainerColor = TvPrimary
                        )
                    }
                }
            }
            is TvViewModel.ShowDetailUiState.Success -> {
                val show: TvShow = state.show
                val episodes: List<TvEpisode> = state.episodes
                val isFav = favorites.any { it.id == show.id }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(300.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val imageUrl = fullMediaUrl(show.displayImageUrl)
                                    if (imageUrl.isNotEmpty()) {
                                        coil.compose.AsyncImage(
                                            model = imageUrl,
                                            contentDescription = show.displayTitle,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF2A2A3A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(show.displayTitle, color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = show.displayTitle,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (show.rating != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = show.rating.toString(), color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                    if (show.status != null) {
                                        Text(text = "Estado: ${show.status}", color = TvAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val genresList: List<String> = show.genres ?: show.genre ?: emptyList<String>()
                                if (genresList.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        genresList.forEach { genreName ->
                                            Surface(
                                                color = Color(0xFF2D2D3D),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = genreName,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Text(
                                    text = show.displayDescription ?: "Sin descripción disponible.",
                                    color = TvOnSurface,
                                    fontSize = 14.sp,
                                    maxLines = 6
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    val streamUrl = show.stream_url ?: show.watch_url
                                    val youtubeId = show.youtube_video_id
                                    
                                    if (!streamUrl.isNullOrBlank() || !youtubeId.isNullOrBlank()) {
                                        TvButton(
                                            onClick = {
                                                viewModel.addShowContinueWatching(show, 0.5f)
                                                if (!streamUrl.isNullOrBlank()) {
                                                    if (show.playback_type == "youtube_embed" && !youtubeId.isNullOrBlank()) {
                                                        launchYoutube(context, youtubeId)
                                                    } else {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl)).apply {
                                                            setDataAndType(Uri.parse(streamUrl), "video/*")
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        try {
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl))
                                                            context.startActivity(webIntent)
                                                        }
                                                    }
                                                } else if (!youtubeId.isNullOrBlank()) {
                                                    launchYoutube(context, youtubeId)
                                                }
                                            },
                                            text = "Reproducir Serie",
                                            focusedContainerColor = TvPrimary
                                        )
                                    }

                                    TvButton(
                                        onClick = { viewModel.toggleFavoriteShow(show) },
                                        text = if (isFav) "Quitar de Mi Lista" else "Añadir a Mi Lista",
                                        focusedContainerColor = TvAccent
                                    )

                                    TvButton(
                                        onClick = { navController.popBackStack() },
                                        text = "Volver",
                                        focusedContainerColor = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    if (episodes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Episodios disponibles",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(episodes) { episode: TvEpisode ->
                                    var isFocused by remember { mutableStateOf(false) }
                                    Card(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .onFocusChanged { isFocused = it.isFocused }
                                            .focusable()
                                            .clickable {
                                                if (!episode.stream_url.isNullOrBlank()) {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(episode.stream_url)).apply {
                                                        setDataAndType(Uri.parse(episode.stream_url), "video/*")
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(episode.stream_url))
                                                        context.startActivity(webIntent)
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Episodio: ${episode.displayTitle}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .border(
                                                width = 2.dp,
                                                color = if (isFocused) Color.White else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF23232F)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .background(Color.Black)
                                            ) {
                                                val epImage = fullMediaUrl(episode.image_url)
                                                if (epImage.isNotEmpty()) {
                                                    coil.compose.AsyncImage(
                                                        model = epImage,
                                                        contentDescription = episode.displayTitle,
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                                    }
                                                }
                                            }
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "T${episode.season ?: 1} E${episode.number ?: 1} - ${episode.displayTitle}",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                if (!episode.summary.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = episode.summary,
                                                        color = TvOnSurface,
                                                        fontSize = 11.sp,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvShowCard(
    show: TvShow,
    onSelected: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1.0f)

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onSelected)
            .border(
                width = 2.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23232F))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val posterUrl = fullMediaUrl(show.displayImageUrl)
            if (posterUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = posterUrl,
                    contentDescription = show.displayTitle,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2A2A3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = show.displayTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
