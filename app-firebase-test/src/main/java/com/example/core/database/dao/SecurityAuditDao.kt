package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.core.database.entity.SecurityAuditEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityAuditDao {

    @Insert
    suspend fun recordAuditLog(log: SecurityAuditEntity): Long

    @Query("SELECT * FROM security_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentAuditLogs(limit: Int = 50): Flow<List<SecurityAuditEntity>>

    @Query("SELECT COUNT(*) FROM security_audit_logs")
    suspend fun countAuditLogs(): Int
}
