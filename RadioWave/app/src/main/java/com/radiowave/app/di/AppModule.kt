package com.radiowave.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.radiowave.app.core.data.local.RadioDatabase
import com.radiowave.app.core.data.remote.RadioBrowserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "radiowave_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor { Timber.d(it) }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    fun provideRadioBrowserApi(client: OkHttpClient): RadioBrowserApi = Retrofit.Builder()
        .baseUrl("https://de1.api.radio-browser.info/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RadioBrowserApi::class.java)

    @Provides
    @Singleton
    fun provideRadioDatabase(@ApplicationContext context: Context): RadioDatabase =
        Room.databaseBuilder(context, RadioDatabase::class.java, "radiowave.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteStationDao(db: RadioDatabase) = db.favoriteStationDao()

    @Provides
    @Singleton
    fun provideRecordingDao(db: RadioDatabase) = db.recordingDao()

    @Provides
    @Singleton
    fun provideScheduledRecordingDao(db: RadioDatabase) = db.scheduledRecordingDao()

    @Provides
    @Singleton
    fun provideListeningHistoryDao(db: RadioDatabase) = db.listeningHistoryDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).build()
}
