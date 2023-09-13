package com.example.resoluteassignment.presentation.authentication

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)