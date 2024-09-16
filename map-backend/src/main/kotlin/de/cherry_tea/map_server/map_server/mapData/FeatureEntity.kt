package de.cherry_tea.map_server.map_server.mapData

data class FeatureEntity(
    val geometry: org.locationtech.jts.geom.Geometry,
    val type: String
)
