package com.kpstv.composetest.data.db.localized

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.composetest.data.models.LocalConfiguration
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(config: LocalConfiguration)

  @Query("delete from table_local_config where id = :id")
  suspend fun delete(id: Int)

  @Query("select * from table_local_config")
  fun getAsFlow(): Flow<List<LocalConfiguration>>
}