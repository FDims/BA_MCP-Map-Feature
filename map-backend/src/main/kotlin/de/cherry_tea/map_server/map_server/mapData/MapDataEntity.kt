package de.cherry_tea.map_server.map_server.mapData

import jakarta.persistence.*

@Entity
@Table(name = "map_areas")
data class MapDataEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @Column(nullable = false)
    val radius: Double,

    @Column(columnDefinition = "TEXT")
    val osmData: String
)