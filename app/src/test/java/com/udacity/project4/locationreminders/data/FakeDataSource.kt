package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private val reminders = mutableListOf<ReminderDTO>()
    private var isReturnError = false

    fun setIsReturnError(value: Boolean) {
        isReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (isReturnError) {
            return Result.Error("Errors while getting reminders")
        }
        return Result.Success(ArrayList(reminders))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (isReturnError) {
            return Result.Error("Errors while getting reminder")
        }
        val reminder = reminders.find {
            it.id == id
        } ?: return Result.Error("Not found reminder")

        return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}