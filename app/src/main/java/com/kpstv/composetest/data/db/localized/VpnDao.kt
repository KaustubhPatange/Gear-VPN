package com.kpstv.composetest.data.db.localized

import androidx.room.*
import com.kpstv.composetest.data.models.VpnConfiguration

@Dao
interface VpnDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(list: List<VpnConfiguration>)

  @Query("delete from table_vpnconfigs")
  suspend fun clearAll()

  @Transaction
  suspend fun insertAll(list: List<VpnConfiguration>) {
    clearAll()
    upsert(list)
  }

  @Query("select * from table_vpnconfigs")
  suspend fun getAll(): List<VpnConfiguration>
}