package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private var database: RemindersDatabase? = null
    private var remindersDao: RemindersDao? = null

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val listDataTest = mutableListOf(
        ReminderDTO("Title 1", "Description1", "Location 1", 12345.0, 12345.0),
        ReminderDTO("Title 2", "Description2", "Location 2", 1122.0, 2233.0),
        ReminderDTO("Title 3", "Description3", "Location 3", 4455.0, 6677.0)
    )

    @Before
    fun initTestConfig() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        remindersDao = database?.reminderDao()
        insertAllReminders()
    }

    @Test
    fun saveAndGetByIdData() = runTest {
        val dataMock =
            ReminderDTO("Title 4", "Description 4", "Location 4", 1111.0, 2222.0)
        remindersDao?.saveReminder(dataMock)
        val actual = remindersDao?.getReminderById(dataMock.id)
        assertThat(actual, `is`(dataMock))
    }

    @Test
    fun getReminders() = runTest {
        val actual = remindersDao?.getReminders()
        assertThat(actual, `is`(listDataTest))
    }

    @Test
    fun deleteAllReminders() = runTest {
        remindersDao?.deleteAllReminders()
        val actual = remindersDao?.getReminders()
        assertThat(actual, `is`(emptyList()))
    }

    private fun insertAllReminders() {
        runBlocking {
            remindersDao?.saveReminder(listDataTest[0])
            remindersDao?.saveReminder(listDataTest[1])
            remindersDao?.saveReminder(listDataTest[2])
        }
    }

}