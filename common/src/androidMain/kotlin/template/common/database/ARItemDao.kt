package template.common.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ARItemDao {
    @Query("SELECT * FROM ar_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ARItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ARItemEntity)

    @Delete
    suspend fun deleteItem(item: ARItemEntity)

    @Query("DELETE FROM ar_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
