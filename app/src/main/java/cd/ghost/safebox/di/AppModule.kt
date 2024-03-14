package cd.ghost.safebox.di

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import cd.ghost.safebox.domain.CipherController
import cd.ghost.safebox.data.DefCipherController
import cd.ghost.safebox.data.DefResources
import cd.ghost.safebox.domain.DirectoryManager
import cd.ghost.safebox.data.FileRepositoryImpl
import cd.ghost.safebox.core.Resources
import cd.ghost.safebox.data.DataStoreOperationsImpl
import cd.ghost.safebox.data.database.AppDatabase
import cd.ghost.safebox.data.database.FileDao
import cd.ghost.safebox.domain.FileRepository
import cd.ghost.safebox.data.DefDirectoryManager
import cd.ghost.safebox.data.DoActivityResultsImpl
import cd.ghost.safebox.domain.GlobalApplicationDirs
import cd.ghost.safebox.data.GlobalApplicationDirsImpl
import cd.ghost.safebox.domain.AppSettings
import cd.ghost.safebox.domain.DoActivityRequests
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [AppModule.BinderModule::class]
)
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
//            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFileDao(
        appDatabase: AppDatabase
    ): FileDao {
        return appDatabase.getFileDao()
    }

    private companion object {
        const val DATABASE_NAME = "safe-box-database"
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface BinderModule {

        @Binds
        @Singleton
        fun bindFileRepository(repo: FileRepositoryImpl): FileRepository

        @Binds
        fun bindCipherController(controller: DefCipherController): CipherController

        @Binds
        fun bindDirectoryManager(directoryUtil: DefDirectoryManager): DirectoryManager

        @Binds
        fun bindGlobalAppDirs(globalAppDirs: GlobalApplicationDirsImpl): GlobalApplicationDirs

        @Binds
        fun bindDoActivityResult(activityReqs: DoActivityResultsImpl): DoActivityRequests

        @Binds
        fun bindAppSettings(dataStore: DataStoreOperationsImpl): AppSettings

        @Binds
        fun bindResources(resources: DefResources): Resources
    }
}