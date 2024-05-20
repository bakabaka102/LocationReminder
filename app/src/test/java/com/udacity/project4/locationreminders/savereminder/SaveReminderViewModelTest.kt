package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var dataSourceTest = FakeDataSource()

    private var viewModel: SaveReminderViewModel? = null

    private val dataTestValid = ReminderDataItem(
        "Data Title", "Data description", "Data location", 12345.0, 12345.0
    )
    private val dataTestInValid = ReminderDataItem(
        null, "Data description", null, 106.0, 107.3
    )

    @Before
    fun initConfig() {
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSourceTest)
    }

    @Test
    fun testSaveReminderInValid() = TestScope().runTest {
        viewModel?.validateAndSaveReminder(dataTestInValid)
        val actual = dataSourceTest.getReminder(dataTestInValid.id)
        Truth.assertThat(actual).isEqualTo(Result.Error("Not found reminder"))
    }

    @Test
    fun saveReminderSuccess() = TestScope().runTest {
        dataSourceTest.deleteAllReminders()
        viewModel?.validateAndSaveReminder(dataTestValid)
        val actual = dataSourceTest.getReminder(dataTestValid.id)
        val expected = Result.Success(
            ReminderDTO(
                title = dataTestValid.title,
                description = dataTestValid.description,
                location = dataTestValid.location,
                latitude = dataTestValid.latitude,
                longitude = dataTestValid.longitude,
                id = dataTestValid.id
            )
        )
        Truth.assertThat(actual).isEqualTo(expected)
    }

}