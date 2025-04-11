package com.example.fxratetracker.domain.model

import arrow.core.Either

sealed interface Failure {
    val exception: Throwable
}

sealed interface LocalFailure : Failure

sealed interface RemoteFailure : Failure

data class UnexpectedFailure(
    override val exception: Throwable,
) : Failure

inline fun <T> Either.Companion.catchUnexpected(action: () -> T): Either<Failure, T> {
    return catch(action).mapLeft { UnexpectedFailure(it) }
}