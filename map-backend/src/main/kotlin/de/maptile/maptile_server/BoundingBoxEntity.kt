package de.maptile.maptile_server

data class BoundingBoxEntity(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double
)
