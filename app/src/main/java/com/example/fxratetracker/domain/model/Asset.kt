package com.example.fxratetracker.domain.model

import kotlinx.serialization.Serializable

// TODO inline class?
typealias AssetCode = String

@Serializable
data class Asset(
    val code: AssetCode,
    val name: String,
)

data class SelectableAsset(
    val asset: Asset,
    val isSelected: Boolean,
)