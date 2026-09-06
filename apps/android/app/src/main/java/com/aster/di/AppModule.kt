package com.aster.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.aster.data.local.SettingsDataStore
import com.aster.data.local.db.AsterDatabase
import com.aster.data.local.db.ToolCallLogDao
import com.aster.data.websocket.AsterWebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aster_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        // Do not ship an HTTP logging interceptor: even BASIC logging can expose
        // user-selected server URLs and query credentials in logs.
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideSettingsDataStore(dataStore: DataStore<Preferences>): SettingsDataStore = SettingsDataStore(dataStore)

    @Provides
    @Singleton
    fun provideWebSocketClient(okHttpClient: OkHttpClient, @ApplicationContext context: Context): AsterWebSocketClient =
        AsterWebSocketClient(okHttpClient, context)

    @Provides
    @Singleton
    fun provideAsterDatabase(@ApplicationContext context: Context): AsterDatabase = Room.databaseBuilder(
        context, AsterDatabase::class.java, "aster_db"
    ).addMigrations(AsterDatabase.MIGRATION_1_2).build()

    @Provides
    @Singleton
    fun provideToolCallLogDao(database: AsterDatabase): ToolCallLogDao = database.toolCallLogDao()
}
