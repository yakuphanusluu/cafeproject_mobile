package com.brewandbean.app.data.model

data class UserData(
    val fullName: String,
    val username: String,
    val email: String,
    val stars: Int = 0
)
