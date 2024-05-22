package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoMatchingRootException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource by lazy { DataBindingIdlingResource() }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun showSnackBarLocationNotInputInfo() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        insertTextHideKeyBoard(R.id.reminderTitle, "Title")
        insertTextHideKeyBoard(R.id.reminderDescription, "Description")
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(appContext.getString(R.string.err_select_location)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun saveLocationToastSuccess() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        insertTextHideKeyBoard(R.id.reminderTitle, "Location")
        insertTextHideKeyBoard(R.id.reminderDescription, "My Location selected")
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.mapFragment)).perform(ViewActions.longClick())
        Espresso.onView(ViewMatchers.withId(R.id.btnSaveLocation)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        var exceptionCaptured = false
        try {
            Espresso.onView(ViewMatchers.withText(R.string.reminder_saved))
                .inRoot(
                    RootMatchers.withDecorView(
                        CoreMatchers.not(CoreMatchers.`is`(activity.window.decorView))
                    )
                )
                .check(ViewAssertions.doesNotExist())
        } catch (e: NoMatchingRootException) {
            exceptionCaptured = true
        }
        assertTrue(exceptionCaptured)
        activityScenario.close()
    }

    private fun insertTextHideKeyBoard(inputId: Int?, text: String?) {
        Espresso.onView(
            inputId?.let { ViewMatchers.withId(it) }
        ).perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())
    }
}
