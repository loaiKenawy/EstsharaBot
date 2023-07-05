package com.example.estsharabot.model


data class User(
    var fullName: String? = null,
    var phone: String? = null,
    var age: Int? = null,
    var gander: Int? = null
) {
    var email: String = ""
    var password: String = ""
    constructor(email: String, password: String) : this("", "", 0, 0) {
        this.email = email
        this.password = password

    }
}