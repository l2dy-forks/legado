package io.legado.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.legado.app.data.entities.AiDictRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AiDictRuleDao {

    @get:Query("select * from aiDictRules order by sortNumber")
    val all: List<AiDictRule>

    @get:Query("select * from aiDictRules where enabled = 1 order by sortNumber")
    val enabled: List<AiDictRule>

    @Query("select * from aiDictRules order by sortNumber")
    fun flowAll(): Flow<List<AiDictRule>>

    @Query("select * from aiDictRules where name = :name")
    fun getByName(name: String): AiDictRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg aiDictRule: AiDictRule)

    @Update
    fun update(vararg aiDictRule: AiDictRule)

    @Delete
    fun delete(vararg aiDictRule: AiDictRule)
}
