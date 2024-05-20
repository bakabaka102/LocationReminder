package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : TestWatcher() {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private var viewModel: RemindersListViewModel? = null
    private var dataSourceTest: FakeDataSource = FakeDataSource()
    private val dataTest = ReminderDTO(
        "Data Title", "Data description", "Data location", 12345.0, 12345.0
    )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDataAndConfig() {
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSourceTest)
    }

    @Test
    fun testLoadingData() = TestScope().runTest {
        dataSourceTest.saveReminder(dataTest)
        viewModel?.loadReminders()
        Truth.assertThat(viewModel?.showLoading?.value).isTrue()
        Truth.assertThat(viewModel?.showLoading?.value).isFalse()
    }

    @Test
    fun testInternalError() = TestScope().runTest {
        dataSourceTest.deleteAllReminders()
        viewModel?.loadReminders()
        assertEquals("Internal errors while getting reminders", viewModel?.showSnackBar?.value)
    }
}