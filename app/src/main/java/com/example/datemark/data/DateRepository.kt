package com.example.datemark.data

import android.content.Context
import android.content.SharedPreferences
import com.example.datemark.model.DateMark
import com.example.datemark.model.Recurrence
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class DateRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    fun getAll(): List<DateMark> {
        val jsonString = prefs.getString(KEY_DATES, null) ?: return emptyList()
        val list = mutableListOf<DateMark>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val label = obj.getString("label")
                val date = LocalDate.parse(obj.getString("date"))
                val note = obj.optString("note", "")
                val recurrence = try {
                    Recurrence.valueOf(obj.optString("recurrence", Recurrence.NONE.name))
                } catch (e: Exception) {
                    Recurrence.NONE
                }
                val createdAt = LocalDate.parse(obj.optString("createdAt", date.toString()))
                val updatedAt = LocalDate.parse(obj.optString("updatedAt", createdAt.toString()))

                list.add(
                    DateMark(
                        id = id,
                        label = label,
                        date = date,
                        note = note,
                        recurrence = recurrence,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun save(dateMark: DateMark) {
        val currentList = getAll().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == dateMark.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = dateMark
        } else {
            currentList.add(dateMark)
        }
        persistList(currentList)
    }

    fun delete(id: String) {
        val currentList = getAll().toMutableList()
        currentList.removeAll { it.id == id }
        persistList(currentList)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_DATES).apply()
    }

    private fun persistList(list: List<DateMark>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("label", item.label)
            obj.put("date", item.date.toString()) // YYYY-MM-DD
            obj.put("note", item.note)
            obj.put("recurrence", item.recurrence.name)
            obj.put("createdAt", item.createdAt.toString())
            obj.put("updatedAt", item.updatedAt.toString())
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_DATES, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "datemark_dates_prefs"
        private const val KEY_DATES = "saved_dates_json"
    }
}
