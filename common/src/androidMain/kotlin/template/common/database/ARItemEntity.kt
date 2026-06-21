package template.common.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ar_items")
data class ARItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetImageUrl: String,
    val contentUrl: String,
    val mindUrl: String,
    val isVideo: Boolean,
    val createdAt: Long
)
