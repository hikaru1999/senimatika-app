package com.LambdaProject.MathArt.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LambdaProject.MathArt.Data.ChallengeRepo
import com.LambdaProject.MathArt.Data.KuesionerRepository
import com.LambdaProject.MathArt.Data.KuesionerRepositoryImpl
import com.LambdaProject.MathArt.Data.QuizResultRepository
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Module
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideQuizResultRepository(): QuizResultRepository {
        return QuizResultRepository()
    }

    @Provides
    @Singleton
    fun provideChallengeRepo(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ChallengeRepo = ChallengeRepo(firestore, auth)

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("validator_session")
        }
    }

    @Provides
    @Singleton
    fun provideKuesionerRepository(
        firestore: FirebaseFirestore,
        dataStore: DataStore<Preferences>
    ): KuesionerRepository {
        return KuesionerRepositoryImpl(firestore, dataStore)
    }
}