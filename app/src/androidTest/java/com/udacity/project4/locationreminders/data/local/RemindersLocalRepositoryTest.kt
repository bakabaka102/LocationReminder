package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import com.udacity.project4.locationreminders.data.dto.Result
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private val data = ReminderDTO("Title 1", "Description1", "Location 1", 12345.0, 12345.0)
    private val data2 = ReminderDTO("Title 2", "Description2", "Location 2", 1122.0, 2233.0)
    private var database: RemindersDatabase? = null
    private var remindersDao: RemindersDao? = null
    private var remindersLocalRepository: RemindersLocalRepository? = null

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initTestConfig() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
        remindersDao = database?.reminderDao()
        remindersLocalRepository = remindersDao?.let {
            RemindersLocalRepository(it, Dispatchers.Unconfined)
        }
    }

    @Test
    fun testGetRemindersWithError() = runTest {
        assertThat(
            remindersLocalRepository?.getReminder("xxx"),
            `is`(Result.Error("Reminder not found!"))
        )
    }

    @Test
    fun testInsertGetRemindersSuccess() = runTest {
        remindersLocalRepository?.saveReminder(data)
        val actual = remindersLocalRepository?.getReminder(data.id)
        assertThat(actual, `is`(Result.Success(data)))
    }

    @Test
    fun testInsertGetAllRemindersSuccess() = runTest {
        remindersLocalRepository?.saveReminder(data)
        remindersLocalRepository?.saveReminder(data2)
        val actual = remindersLocalRepository?.getReminders()
        assertThat(actual, `is`(Result.Success(listOf(data, data2))))
    }

    @Test
    fun testDeleteSuccess() = runTest {
        remindersLocalRepository?.saveReminder(data)
        remindersLocalRepository?.deleteAllReminders()
        val actual = remindersLocalRepository?.getReminders()
        assertThat(actual, `is`(Result.Success(listOf())))
    }

}