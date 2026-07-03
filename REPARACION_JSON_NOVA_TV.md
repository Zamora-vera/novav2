# Reparación JSON Nova TV Android TV

Esta versión corrige la adaptación de la app Kotlin al JSON actual del backend `https://delivery.allsender.tech`.

## Cambios aplicados

- `content-config` ahora lee la estructura real:
  - `success`
  - `config.app`
  - `config.providers`
  - `config.countries[]`
- `country-content` ahora lee la estructura real:
  - `country_config`
  - `content.sections[]`
  - `sections[].items[]`
- `country-search` ahora lee la respuesta real con `results[]` y la adapta en la app a canales, películas, series, novelas y trailers.
- `shows/{id}` ahora soporta `episodes[]` dentro de la misma respuesta.
- `shows/{id}/episodes` ahora espera `TvEpisodesResponse` y no una lista directa.
- `tvmaze/schedule` ahora espera `TvScheduleResponse`.
- Se agregó `TolerantJsonAdapters` para evitar que la app se caiga si un campo llega como número, texto, boolean o vacío.
- Se agregaron modelos compatibles con EC, DO, HT e IT.
- Se mantuvo app sin login y sin API keys dentro de Kotlin.

## Endpoints principales usados por Kotlin

```txt
GET /api/tv/app-config
GET /api/tv/content-config
GET /api/tv/country-content?country=EC
GET /api/tv/channels?country=EC
GET /api/tv/ads?screen=home&platform=android_tv&country=EC
GET /api/tv/country-search?country=EC&query=avatar&limit=30
GET /api/tv/search/movies?query=avatar&limit=20
GET /api/tv/search/shows?query=Breaking%20Bad&limit=20
GET /api/tv/shows/tvmaze-169
GET /api/tv/shows/tvmaze-169/episodes
GET /api/tv/trailers/kinocheck?language=en&limit=20
```

## Países esperados

- EC Ecuador
- DO República Dominicana
- HT Haití
- IT Italia

## Nota de compilación

Abrir este proyecto en Android Studio o Codemagic. No colocar claves de TMDB, TVmaze, Watchmode ni KinoCheck en Kotlin. Todo debe pasar por el backend.
