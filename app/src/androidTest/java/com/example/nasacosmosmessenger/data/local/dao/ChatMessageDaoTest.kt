package com.example.nasacosmosmessenger.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.nasacosmosmessenger.data.local.database.AppDatabase
import com.example.nasacosmosmessenger.data.local.entity.ChatMessageEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ChatMessageDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ChatMessageDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.chatMessageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieve() = runTest {
        val message = ChatMessageEntity(
            id = "test-1",
            content = "Hello",
            apodDate = null,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )

        dao.insert(message)
        val messages = dao.getAll()

        assertThat(messages).hasSize(1)
        assertThat(messages[0].content).isEqualTo("Hello")
    }

    @Test
    fun messagesOrderedByTimestamp() = runTest {
        val older = ChatMessageEntity("1", "First", null, true, 1000L)
        val newer = ChatMessageEntity("2", "Second", null, false, 2000L)

        dao.insert(newer)
        dao.insert(older)

        val messages = dao.getAll()

        assertThat(messages[0].content).isEqualTo("First")
        assertThat(messages[1].content).isEqualTo("Second")
    }

    @Test
    fun deleteAllClearsMessages() = runTest {
        dao.insert(ChatMessageEntity("1", "Test", null, true, 1000L))
        dao.deleteAll()

        assertThat(dao.getCount()).isEqualTo(0)
    }
}
