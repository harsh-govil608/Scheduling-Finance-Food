package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.lifeos.expensecapture.data.db.entity.CorrectionEntity

@Dao
interface CorrectionDao {

    @Insert
    suspend fun insert(correction: CorrectionEntity)
}
