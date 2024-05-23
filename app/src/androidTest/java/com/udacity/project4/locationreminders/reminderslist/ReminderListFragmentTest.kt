package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private var dataSource: ReminderDataSource? = null
    private lateinit var application: Application
    private val data = ReminderDTO("Title 1", "Description1", "Location 1", 12345.0, 12345.0)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val viewModelModule = module {
        viewModel {
            RemindersListViewModel(application, get() as ReminderDataSource)
        }
        single<SaveReminderViewModel> {
            SaveReminderViewModel(application, get() as ReminderDataSource)
        }
    }
    private val dataModule = module {
        single<ReminderDataSource> {
            RemindersLocalRepository(get())
        }
        single<RemindersDao> {
            LocalDB.createRemindersDao(application)
        }
    }

    @Before
    fun initConfig() {
        application = ApplicationProvider.getApplicationContext()
        stopKoin()
        startKoin {
            modules(viewModelModule, dataModule)
        }
        dataSource = GlobalContext.get().get()
    }

    @Test
    fun displayReminderList(): Unit = runBlocking {
        dataSource?.deleteAllReminders()
        dataSource?.saveReminder(data)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.reminderCardView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun navigationScreen() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            it.view?.let { view -> Navigation.setViewNavController(view, navController) }
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}