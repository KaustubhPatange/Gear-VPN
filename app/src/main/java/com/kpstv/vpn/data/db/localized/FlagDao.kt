package com.kpstv.vpn.data.db.localized

import androidx.room.*
import com.kpstv.vpn.data.models.Flag
import kotlinx.coroutines.flow.Flow

@Dao
interface FlagDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(flag: Flag)

  @Transaction
  suspend fun insert(flags: List<Flag>) {
    flags.forEach { insert(it) }
  }

  @Query("select count(1) where exists (select * from ${Flag.TABLE_NAME})")
  suspend fun isEmpty() : Boolean

  @Query("select * from ${Flag.TABLE_NAME} where country = :country limit 1")
  fun getByCountryFlow(country: String) : Flow<Flag?>

  @Query("select * from ${Flag.TABLE_NAME} where country = :country limit 1")
  suspend fun getByCountry(country: String) : Flag?

  @Query("select * from ${Flag.TABLE_NAME}")
  fun getAllFlags(): Flow<List<Flag>>
}