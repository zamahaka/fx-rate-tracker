package com.example.fxratetracker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AssetsResponse(
    val currencies: Map<String, String>,
)