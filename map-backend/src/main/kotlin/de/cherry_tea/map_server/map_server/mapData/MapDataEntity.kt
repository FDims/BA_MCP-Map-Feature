package de.cherry_tea.map_server.map_server.mapData

import jakarta.persistence.*

data class MapDataEntity(
    val zoom: Int,
    val x: Int,
    val y: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapDataEntity

        if (zoom != other.zoom) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = zoom
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + data.contentHashCode()
        return result
    }
}