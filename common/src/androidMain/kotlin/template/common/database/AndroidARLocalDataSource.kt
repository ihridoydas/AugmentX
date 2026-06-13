package template.common.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import template.common.network.ManagedARItem

class AndroidARLocalDataSource(private val dao: ARItemDao) : ARLocalDataSource {
    override fun getAllItems(): Flow<List<ManagedARItem>> {
        return dao.getAllItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertItem(item: ManagedARItem) {
        dao.insertItem(item.toEntity())
    }

    override suspend fun deleteItem(id: String) {
        dao.deleteById(id)
    }

    private fun ARItemEntity.toDomain() = ManagedARItem(
        id = id,
        name = name,
        targetImageUrl = targetImageUrl,
        contentUrl = contentUrl,
        mindUrl = mindUrl,
        isVideo = isVideo,
        createdAt = createdAt,
        imageUploaded = true,
        contentUploaded = true,
        mindGenerated = true
    )

    private fun ManagedARItem.toEntity() = ARItemEntity(
        id = id,
        name = name,
        targetImageUrl = targetImageUrl,
        contentUrl = contentUrl,
        mindUrl = mindUrl,
        isVideo = isVideo,
        createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt
    )
}
