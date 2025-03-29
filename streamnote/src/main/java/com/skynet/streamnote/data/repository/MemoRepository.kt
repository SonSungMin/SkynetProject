package com.skynet.streamnote.data.repository

import com.skynet.streamnote.data.dao.MemoDao
import com.skynet.streamnote.data.entity.Memo
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDao) {
    fun getAllMemos(): Flow<List<Memo>> = memoDao.getAllMemos()

    fun getActiveMemos(): Flow<List<Memo>> = memoDao.getActiveMemos()

    suspend fun insertMemo(memo: Memo): Long = memoDao.insertMemo(memo)

    suspend fun updateMemo(memo: Memo) = memoDao.updateMemo(memo)

    suspend fun deleteMemo(memo: Memo) = memoDao.deleteMemo(memo)
}