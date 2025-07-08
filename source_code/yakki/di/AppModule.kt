package com.yakki.di

import android.content.Context
import androidx.room.Room
import com.yakki.audio.AudioRecorderImpl
import com.yakki.audio.IAudioRecorder
import com.yakki.data.LanguageDao
import com.yakki.data.YakkiDatabase
import com.yakki.device.AudioDeviceManagerImpl
import com.yakki.player.AudioPlayerManagerImpl
import com.yakki.device.IAudioDeviceManager
import com.yakki.player.IAudioPlayerManager
import com.yakki.repository.LanguageRepository
import com.yakki.repository.LanguageRepositoryImpl
import com.yakki.tts.ITtsManager
import com.yakki.tts.TtsManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /* ────────── Dispatcher ────────── */
    @Provides @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /* ────────── DataBase / DAO ────────── */
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): YakkiDatabase =
        Room.databaseBuilder(ctx, YakkiDatabase::class.java, "yakki.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideLanguageDao(db: YakkiDatabase): LanguageDao = db.languageDao()

    /* ────────── Repository ────────── */
    @Provides @Singleton
    fun provideLanguageRepository(dao: LanguageDao): LanguageRepository =
        LanguageRepositoryImpl(dao)                      // ✔︎ 1 аргумент

    /* ────────── Audio ────────── */
    @Provides @Singleton
    fun provideAudioRecorder(@ApplicationContext ctx: Context): IAudioRecorder =
        AudioRecorderImpl(ctx)                           // ✔︎ 1 аргумент

    @Provides @Singleton
    fun provideAudioPlayerManager(@ApplicationContext ctx: Context): IAudioPlayerManager =
        AudioPlayerManagerImpl(ctx)

    @Provides @Singleton
    fun provideAudioDeviceManager(@ApplicationContext ctx: Context): IAudioDeviceManager =
        AudioDeviceManagerImpl(ctx)

    /* ────────── TTS ────────── */
    @Provides @Singleton
    fun provideTtsManager(@ApplicationContext ctx: Context): ITtsManager =
        TtsManagerImpl(ctx)
}
