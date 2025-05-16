package com.LambdaProject.MathArt.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LambdaProject.MathArt.Data.ChallengeRepo
import com.LambdaProject.MathArt.Data.QuizResultRepository
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Module

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
}