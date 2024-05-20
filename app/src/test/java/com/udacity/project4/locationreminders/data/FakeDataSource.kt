package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //TODO: Create a fake data source to act as a double to the real data source
    private val reminders = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(mutableListOf())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.find {
            it.id == id
        } ?: return Result.Error("Not found reminder")

        return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}