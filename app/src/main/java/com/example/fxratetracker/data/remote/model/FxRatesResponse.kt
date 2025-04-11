package com.example.fxratetracker.data.remote.model

import com.example.fxratetracker.data.remote.BigDecimalJson
import kotlinx.serialization.Serializable

@Serializable
data class FxRatesResponse(
    val timestamp: Int,
    val source: String,
    val quotes: Map<String, BigDecimalJson>,
)