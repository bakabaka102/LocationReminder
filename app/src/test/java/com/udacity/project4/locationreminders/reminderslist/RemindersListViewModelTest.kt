package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : TestWatcher() {

    private var viewModel: RemindersListViewModel? = null
    private var dataSourceTest: FakeDataSource = FakeDataSource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initDataAndConfig() {
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSourceTest)
    }

    @Test
    fun loadingIcon_Task() = TestScope().runTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel?.loadReminders()
        MatcherAssert.assertThat(viewModel?.showLoading?.value, Matchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(viewModel?.showLoading?.value, Matchers.`is`(false))
    }

    @Test
    fun loadingNoData() = TestScope().runTest {
        dataSourceTest.deleteAllReminders()
        viewModel?.loadReminders()
        Truth.assertThat(viewModel?.showNoData?.value).isEqualTo(true)
    }

    @Test
    fun loadingDataSuccess() = TestScope().runTest {
        dataSourceTest.deleteAllReminders()
        viewModel?.loadReminders()
        Truth.assertThat(viewModel?.showNoData?.value).isEqualTo(true)
    }

    @Test
    fun isReturnError() = TestScope().runTest {
        dataSourceTest.setIsReturnError(true)
        viewModel?.loadReminders()
        assertEquals("Errors while getting reminders", viewModel?.showSnackBar?.value)
    }
}