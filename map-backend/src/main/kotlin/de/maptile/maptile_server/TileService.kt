package de.maptile.maptile_server

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.math.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Service
class MapService {
    private val tileServerUrl = "https://tile.openstreetmap.org"
    private val restTemplate = RestTemplate()
    private val tileSize = 256

    fun getSitePlanImageForArea(lat: Double, lon: Double, radiusMeters: Double): ByteArray {
        val zoom = calculateZoomLevel(radiusMeters)
        val bbox = calculateBoundingBox(lat, lon, radiusMeters)
        val tiles = getTilesInBoundingBox(bbox, zoom)

        val minX = tiles.minOf { it.lon }
        val maxX = tiles.maxOf { it.lon }
        val minY = tiles.minOf { it.lat }
        val maxY = tiles.maxOf { it.lat }

        val width = (maxX - minX + 1) * tileSize
        val height = (maxY - minY + 1) * tileSize

        val combinedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = combinedImage.createGraphics()
        tiles.forEach { tileCoord ->
            val tileImage = fetchTileImage(tileCoord)
            g.drawImage(
                tileImage, ((tileCoord.lon - minX) * tileSize),
                ((tileCoord.lat - minY) * tileSize), null
            )
        }

        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(combinedImage, "png", outputStream)
        val imageData = outputStream.toByteArray()

        return imageData
    }

    fun getSitePlanImageForAreaByName(placeName: String, radiusMeters: Double): ByteArray {
        val url = "https://nominatim.openstreetmap.org/search?q=$placeName&format=json&limit=1"
        val response = restTemplate.getForObject(url, Array<CoordinateEntity>::class.java)

        if (!response.isNullOrEmpty()) {
            val location = response[0]
            val lat = location.lat
            val lon = location.lon
            return getSitePlanImageForArea(lat, lon, radiusMeters)
        }

        throw error("Lagename kann nicht gefunden werden.")
    }

    // util function
    private fun calculateZoomLevel(radiusMeters: Double): Int {
        val zoom = (16 - log2(radiusMeters / 1000)).toInt()
        return zoom
    }

    private fun calculateBoundingBox(lat: Double, lon: Double, radiusMeters: Double): BoundingBoxEntity {
        val latRadians = Math.toRadians(lat)
        val lonRadians = Math.toRadians(lon)
        val earthRadius = 6378137.0 // in meters

        val latDelta = radiusMeters / earthRadius
        val lonDelta = asin(sin(radiusMeters / earthRadius) / cos(latRadians))

        val minLat = Math.toDegrees(latRadians - latDelta)
        val maxLat = Math.toDegrees(latRadians + latDelta)
        val minLon = Math.toDegrees(lonRadians - lonDelta)
        val maxLon = Math.toDegrees(lonRadians + lonDelta)

        return BoundingBoxEntity(minLat, minLon, maxLat, maxLon)
    }

    private fun getTilesInBoundingBox(bbox: BoundingBoxEntity, zoom: Int): List<TileCoordinateEntity> {
        val minX = lon2tile(bbox.minLon, zoom)
        val maxX = lon2tile(bbox.maxLon, zoom)
        val minY = lat2tile(bbox.maxLat, zoom)
        val maxY = lat2tile(bbox.minLat, zoom)

        return (minX..maxX).flatMap { x ->
            (minY..maxY).map { y ->
                TileCoordinateEntity(x, y, zoom)
            }
        }
    }

    private fun fetchTileImage(tileCoord: TileCoordinateEntity): BufferedImage? {
        val url = "$tileServerUrl/${tileCoord.zoom}/${tileCoord.lon}/${tileCoord.lat}.png"

        val headers = HttpHeaders()
        headers.set("User-Agent", "MCP/1.0 (info@mission-control-paramedic.de)")

        val entity = HttpEntity<ByteArray>(headers)

        val response: ResponseEntity<ByteArray> = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            ByteArray::class.java
        )

        return response.body?.let { ImageIO.read(it.inputStream()) }
    }

    private fun lon2tile(lon: Double, zoom: Int) = ((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()

    private fun lat2tile(lat: Double, zoom: Int): Int {
        val latRad = Math.toRadians(lat)
        return ((1.0 - asinh(tan(latRad)) / PI) / 2.0 * (1 shl zoom)).toInt()
    }
}
