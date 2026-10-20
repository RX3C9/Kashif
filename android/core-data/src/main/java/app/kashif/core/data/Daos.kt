package app.kashif.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockRuleDao {
    @Query("SELECT * FROM BlockRule ORDER BY createdAt DESC") fun observeAll(): Flow<List<BlockRule>>
    @Query("SELECT * FROM BlockRule WHERE enabled = 1") suspend fun enabled(): List<BlockRule>
    @Insert suspend fun insert(rule: BlockRule): Long
    @Upsert suspend fun upsert(rule: BlockRule)
    @Delete suspend fun delete(rule: BlockRule)
}

@Dao
interface SpamDao {
    @Query("SELECT * FROM SpamEntry WHERE hash = :hash LIMIT 1") suspend fun byHash(hash: String): SpamEntry?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(rows: List<SpamEntry>)
    @Upsert suspend fun upsert(row: SpamEntry)
    @Query("SELECT COUNT(*) FROM SpamEntry") suspend fun count(): Int
}

@Dao
interface ReportDao {
    @Insert suspend fun insert(r: LocalReport): Long
    @Query("SELECT * FROM LocalReport ORDER BY createdAt DESC") fun observeAll(): Flow<List<LocalReport>>
    @Query("SELECT * FROM LocalReport WHERE synced = 0") suspend fun unsynced(): List<LocalReport>
}

@Dao
interface CallNoteDao {
    @Query("SELECT * FROM CallNote WHERE e164 = :e164") suspend fun forNumber(e164: String): List<CallNote>
    @Insert suspend fun insert(n: CallNote): Long
}
