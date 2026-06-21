package template.common.database

import kotlinx.coroutines.flow.Flow
import template.common.network.ManagedARItem

interface ARLocalDataSource {
    fun getAllItems(): Flow<List<ManagedARItem>>
    suspend fun insertItem(item: ManagedARItem)
    suspend fun deleteItem(id: String)
}
