package com.example.kidsenglishlab.data

import org.json.JSONArray
import org.json.JSONObject

data class UserProfile(
    val id: String,
    val name: String,
    val avatar: String = "🦁",
    val completedWords: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("avatar", avatar)
        val arr = JSONArray()
        completedWords.forEach { arr.put(it) }
        obj.put("completedWords", arr)
        obj.put("createdAt", createdAt)
        return obj
    }

    companion object {
        val AVATAR_OPTIONS = listOf("🦁", "🚀", "🐱", "🐻", "🦄", "🌟", "🐶", "👑", "🐼", "🦊", "🦕", "🎨")

        fun fromJson(json: JSONObject): UserProfile {
            val id = json.optString("id", "profile_${System.currentTimeMillis()}")
            val name = json.optString("name", "Küçük Kaşif")
            val avatar = json.optString("avatar", "🦁")
            val createdAt = json.optLong("createdAt", System.currentTimeMillis())
            val completed = mutableSetOf<String>()
            val arr = json.optJSONArray("completedWords")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    completed.add(arr.getString(i))
                }
            }
            return UserProfile(
                id = id,
                name = name,
                avatar = avatar,
                completedWords = completed,
                createdAt = createdAt
            )
        }

        fun defaultProfile(): UserProfile = UserProfile(
            id = "profile_1",
            name = "Küçük Kaşif",
            avatar = "🦁",
            completedWords = emptySet()
        )
    }
}
