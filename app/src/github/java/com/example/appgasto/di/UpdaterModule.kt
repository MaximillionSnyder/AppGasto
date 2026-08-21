package com.example.appgasto.di

import com.example.appgasto.data.updater.GitHubUpdateManager
import com.example.appgasto.data.updater.UpdateManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdaterModule {

    @Binds
    @Singleton
    abstract fun bindUpdateManager(impl: GitHubUpdateManager): UpdateManager
}
