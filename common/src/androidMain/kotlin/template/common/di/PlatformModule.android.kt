package template.common.di

import androidx.room.Room
import org.koin.dsl.module
import template.common.database.ARDatabase
import template.common.database.ARLocalDataSource
import template.common.database.AndroidARLocalDataSource

actual val platformModule = module {
    single {
        Room.databaseBuilder(
            get(),
            ARDatabase::class.java,
            "augmentx.db"
        ).build()
    }
    single { get<ARDatabase>().arItemDao() }
    single<ARLocalDataSource> { AndroidARLocalDataSource(get()) }
}
