package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.LocalFavoriteChannel
import com.example.data.model.LocalMostWatched
import com.example.data.model.LocalWatchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteChannelDao {
    @Query("SELECT * FROM favorite_channels ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<LocalFavoriteChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: LocalFavoriteChannel)

    @Query("DELETE FROM favorite_channels WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_channels WHERE id = :id LIMIT 1)")
    fun observeIsFavorite(id: String): Flow<Boolean>
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT 20")
    fun getWatchHistory(): Flow<List<LocalWatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: LocalWatchHistory)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

@Dao
interface MostWatchedDao {
    @Query("SELECT * FROM most_watched_channels ORDER BY viewCount DESC, lastWatchedAt DESC")
    fun getMostWatched(): Flow<List<LocalMostWatched>>

    @Query("SELECT * FROM most_watched_channels WHERE id = :id LIMIT 1")
    suspend fun getMostWatchedById(id: String): LocalMostWatched?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMostWatched(mostWatched: LocalMostWatched)

    @Query("UPDATE most_watched_channels SET viewCount = viewCount + 1, lastWatchedAt = :lastWatched WHERE id = :id")
    suspend fun incrementViewCount(id: String, lastWatched: Long)
}

@Dao
interface FavoriteShowDao {
    @Query("SELECT * FROM favorite_shows ORDER BY addedAt DESC")
    fun getAllFavoriteShows(): Flow<List<com.example.data.model.LocalFavoriteShow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteShow(show: com.example.data.model.LocalFavoriteShow)

    @Query("DELETE FROM favorite_shows WHERE id = :id")
    suspend fun deleteFavoriteShowById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_shows WHERE id = :id LIMIT 1)")
    fun observeIsFavoriteShow(id: String): Flow<Boolean>
}

@Dao
interface FavoriteMovieDao {
    @Query("SELECT * FROM favorite_movies ORDER BY addedAt DESC")
    fun getAllFavoriteMovies(): Flow<List<com.example.data.model.LocalFavoriteMovie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteMovie(movie: com.example.data.model.LocalFavoriteMovie)

    @Query("DELETE FROM favorite_movies WHERE id = :id")
    suspend fun deleteFavoriteMovieById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE id = :id LIMIT 1)")
    fun observeIsFavoriteMovie(id: String): Flow<Boolean>
}

@Dao
interface ContinueWatchingDao {
    @Query("SELECT * FROM continue_watching ORDER BY lastWatchedAt DESC")
    fun getContinueWatching(): Flow<List<com.example.data.model.LocalContinueWatching>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinueWatching(item: com.example.data.model.LocalContinueWatching)

    @Query("DELETE FROM continue_watching WHERE id = :id")
    suspend fun deleteContinueWatchingById(id: String)
}

@Database(
    entities = [
        LocalFavoriteChannel::class,
        LocalWatchHistory::class,
        LocalMostWatched::class,
        com.example.data.model.LocalFavoriteShow::class,
        com.example.data.model.LocalFavoriteMovie::class,
        com.example.data.model.LocalContinueWatching::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteChannelDao(): FavoriteChannelDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun mostWatchedDao(): MostWatchedDao
    abstract fun favoriteShowDao(): FavoriteShowDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao
    abstract fun continueWatchingDao(): ContinueWatchingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nova_tv_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
