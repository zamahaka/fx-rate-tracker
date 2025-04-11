package com.example.fxratetracker.domain.model

sealed interface Failure {
    val exception: Exception
}

sealed interface LocalFailure : Failure

sealed interface RemoteFailure : Failure

data class UnexpectedFailure(
    override val exception: Exception,
) : Failure