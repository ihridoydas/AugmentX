package template.common.di

import org.koin.dsl.module
import template.common.database.ARLocalDataSource
import kotlinx.coroutines.flow.flowOf
import template.common.network.ManagedARItem

actual val platformModule = module {
    single<ARLocalDataSource> {
        object : ARLocalDataSource {
            override fun getAllItems() = flowOf(emptyList<ManagedARItem>())
            override suspend fun insertItem(item: ManagedARItem) {}
            override suspend fun deleteItem(id: String) {}
        }
    }
}
