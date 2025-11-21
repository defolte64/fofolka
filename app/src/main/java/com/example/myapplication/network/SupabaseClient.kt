package com.example.myapplication.network

import com.example.myapplication.model.Habit
import com.example.myapplication.utils.SharedPreferencesManager
import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray

object SupabaseClient {
    var currentUserId: String = ""
    var currentEmail: String = ""

    private val SUPABASE_URL = "https://kbilrjunpgwujusnmvek.supabase.co"
    private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtiaWxyanVucGd3dWp1c25tdmVrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM1NjQ1MDYsImV4cCI6MjA3OTE0MDUwNn0.D9SvBe6Ktd8WoFaWxAUYKch3Pl_yzNZcOpd_ArgY8TA"

    private val mainHandler = Handler(Looper.getMainLooper())

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            callback(false, "Email не может быть пустым")
            return
        }
        if (!email.contains("@")) {
            callback(false, "Введите корректный email")
            return
        }
        if (password.isBlank()) {
            callback(false, "Пароль не может быть пустым")
            return
        }
        if (password.length !in 6..15) {
            callback(false, "Пароль должен быть от 6 до 15 символов")
            return
        }

        Thread {
            try {
                val url = URL("$SUPABASE_URL/auth/v1/signup")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val body = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                connection.outputStream.write(body.toByteArray())

                val responseCode = connection.responseCode
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream.bufferedReader().readText()
                }

                Log.d("Supabase", "SignUp Response: $responseCode - $responseBody")

                if (responseCode == 200) {
                    try {
                        val json = JSONObject(responseBody)
                        val user = json.optJSONObject("user")
                        if (user != null) {
                            currentUserId = user.optString("id", "")
                            currentEmail = email
                            Log.d("Supabase", "SignUp Success! UserID: $currentUserId")


                            SharedPreferencesManager.saveUser(currentUserId, currentEmail)

                            mainHandler.post { callback(true, null) }
                        } else {
                            mainHandler.post { callback(false, "Проверьте вашу почту и подтвердите регистрацию по ссылке") }
                        }
                    } catch (e: Exception) {
                        Log.e("Supabase", "SignUp Parse Error: ${e.message}")
                        mainHandler.post { callback(false, "Ошибка парсинга ответа регистрации") }
                    }
                } else {
                    mainHandler.post { callback(false, "Ошибка регистрации: код $responseCode") }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "SignUp Exception: ${e.message}", e)
                mainHandler.post { callback(false, e.message) }
            }
        }.start()
    }

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            callback(false, "Email не может быть пустым")
            return
        }
        if (!email.contains("@")) {
            callback(false, "Введите корректный email")
            return
        }
        if (password.isBlank()) {
            callback(false, "Пароль не может быть пустым")
            return
        }

        Thread {
            try {
                val url = URL("$SUPABASE_URL/auth/v1/token?grant_type=password")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val body = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                connection.outputStream.write(body.toByteArray())

                val responseCode = connection.responseCode
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream.bufferedReader().readText()
                }

                Log.d("Supabase", "SignIn Response: $responseCode")
                Log.d("Supabase", "SignIn Body: $responseBody")

                if (responseCode == 200) {
                    try {
                        val json = JSONObject(responseBody)
                        val user = json.optJSONObject("user")
                        if (user != null) {
                            currentUserId = user.optString("id", "")
                            currentEmail = email
                            Log.d("Supabase", "SignIn Success! UserID: $currentUserId, Email: $currentEmail")

                            SharedPreferencesManager.saveUser(currentUserId, currentEmail)

                            mainHandler.post { callback(true, null) }
                        } else {
                            Log.e("Supabase", "No user in response")
                            mainHandler.post { callback(false, "Пользователь не найден в ответе") }
                        }
                    } catch (e: Exception) {
                        Log.e("Supabase", "SignIn Parse Error: ${e.message}")
                        mainHandler.post { callback(false, "Ошибка парсинга ответа") }
                    }
                } else {
                    Log.e("Supabase", "SignIn failed with code: $responseCode")
                    mainHandler.post { callback(false, "Неверный email или пароль (код $responseCode)") }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "SignIn Exception: ${e.message}", e)
                mainHandler.post { callback(false, e.message) }
            }
        }.start()
    }

    fun getHabits(userId: String, callback: (List<Habit>?, String?) -> Unit) {
        Log.d("Supabase", "getHabits called with userId: $userId")

        if (userId.isBlank()) {
            callback(null, "userId пуст")
            return
        }

        Thread {
            try {
                val url = URL("$SUPABASE_URL/rest/v1/habits?user_id=eq.$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream.bufferedReader().readText()
                }

                Log.d("Supabase", "getHabits Response Code: $responseCode")
                Log.d("Supabase", "getHabits Response Body: $responseBody")

                if (responseCode == 200) {
                    try {
                        val jsonArray = JSONArray(responseBody)
                        val habitsList = mutableListOf<Habit>()

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.optInt("id", 0)
                            val name = jsonObject.optString("name", "")
                            val checkedDatesArray = jsonObject.optJSONArray("checked_dates")

                            val checkedDates = mutableListOf<String>()
                            if (checkedDatesArray != null) {
                                for (j in 0 until checkedDatesArray.length()) {
                                    checkedDates.add(checkedDatesArray.getString(j))
                                }
                            }

                            val habit = Habit(
                                id = id,
                                name = name,
                                checkedDates = checkedDates
                            )
                            habitsList.add(habit)
                            Log.d("Supabase", "Parsed habit: $habit")
                        }

                        Log.d("Supabase", "Total habits loaded: ${habitsList.size}")
                        mainHandler.post { callback(habitsList, null) }
                    } catch (e: Exception) {
                        Log.e("Supabase", "Parse error: ${e.message}")
                        mainHandler.post { callback(null, "Ошибка парсинга: ${e.message}") }
                    }
                } else {
                    Log.e("Supabase", "Failed to load habits: $responseCode")
                    mainHandler.post { callback(null, "Ошибка загрузки (код $responseCode)") }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "getHabits Exception: ${e.message}", e)
                mainHandler.post { callback(null, e.message) }
            }
        }.start()
    }

    fun addHabit(userId: String, name: String, callback: (Boolean, String?) -> Unit) {
        Log.d("Supabase", "=== addHabit START ===")
        Log.d("Supabase", "userId: $userId")
        Log.d("Supabase", "name: $name")

        if (name.isBlank()) {
            Log.e("Supabase", "name is blank")
            callback(false, "Название привычки не может быть пустым")
            return
        }

        if (userId.isBlank()) {
            Log.e("Supabase", "userId is blank!")
            callback(false, "Ошибка: userId пуст, не авторизован")
            return
        }

        Thread {
            try {
                val url = URL("$SUPABASE_URL/rest/v1/habits")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Prefer", "return=representation")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val body = JSONObject().apply {
                    put("user_id", userId)
                    put("name", name)
                    put("checked_dates", JSONArray())
                }.toString()

                Log.d("Supabase", "Request Body: $body")

                connection.outputStream.write(body.toByteArray())
                connection.outputStream.flush()

                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream.bufferedReader().readText()
                }

                Log.d("Supabase", "addHabit Response Code: $responseCode")
                Log.d("Supabase", "addHabit Response Body: $responseBody")

                if (responseCode in 200..299) {
                    Log.d("Supabase", "Привычка добавлена успешно!")
                    mainHandler.post { callback(true, null) }
                } else {
                    Log.e("Supabase", "Ошибка добавления: $responseCode - $responseBody")
                    mainHandler.post { callback(false, "Ошибка (код $responseCode): $responseBody") }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "addHabit Exception: ${e.message}", e)
                e.printStackTrace()
                mainHandler.post { callback(false, "Исключение: ${e.message}") }
            }
        }.start()
    }

    fun updateHabitDates(habitId: Int, userId: String, dates: List<String>, callback: (Boolean, String?) -> Unit) {
        Log.d("Supabase", "updateHabitDates called with habitId: $habitId, dates: $dates")

        Thread {
            try {
                val url = URL("$SUPABASE_URL/rest/v1/habits?id=eq.$habitId&user_id=eq.$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PATCH"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val body = JSONObject().apply {
                    put("checked_dates", JSONArray(dates))
                }.toString()

                Log.d("Supabase", "Update body: $body")

                connection.outputStream.write(body.toByteArray())
                connection.outputStream.flush()

                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream.bufferedReader().readText()
                }

                Log.d("Supabase", "updateHabitDates Response: $responseCode - $responseBody")

                if (responseCode in 200..299) {
                    Log.d("Supabase", "Даты обновлены!")
                    mainHandler.post { callback(true, null) }
                } else {
                    mainHandler.post { callback(false, "Ошибка обновления: код $responseCode") }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "updateHabitDates Exception: ${e.message}")
                mainHandler.post { callback(false, e.message) }
            }
        }.start()
    }

    fun deleteHabit(habitId: Int, userId: String, callback: (Boolean, String?) -> Unit) {
        Log.d("Supabase", "deleteHabit called with habitId: $habitId, userId: $userId")

        Thread {
            try {
                val url = URL("$SUPABASE_URL/rest/v1/habits?id=eq.$habitId&user_id=eq.$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream.bufferedReader().readText()
                }

                Log.d("Supabase", "deleteHabit Response: $responseCode - $responseBody")

                if (responseCode in 200..299) {
                    Log.d("Supabase", "Привычка удалена!")
                    mainHandler.post { callback(true, null) }
                } else {
                    mainHandler.post { callback(false, "Ошибка удаления: код $responseCode") }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "deleteHabit Exception: ${e.message}")
                mainHandler.post { callback(false, e.message) }
            }
        }.start()
    }

    fun markHabitChecked(habitId: Int, date: String, callback: (Boolean, String?) -> Unit) {
        mainHandler.post { callback(true, null) }
    }

    fun signOut(callback: (Boolean) -> Unit) {
        currentUserId = ""
        currentEmail = ""
        SharedPreferencesManager.logout()
        Log.d("Supabase", "User signed out")
        mainHandler.post { callback(true) }
    }
}




