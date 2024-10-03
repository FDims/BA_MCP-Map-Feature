package de.maptile.maptile_server

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.math.*
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO

@Service
class MapService {
    private val tileServerUrl = "https://tile.openstreetmap.org"
    private val restTemplate = RestTemplate()
    private val tileSize = 256 // OpenStreetMap uses 256x256 pixel tiles
    private val log = LoggerFactory.getLogger(MapService::class.java)

    fun getMapImageForArea(lat: Double, lon: Double, radiusMeters: Double, minZoom: Int?, maxZoom: Int?): ByteArray {
        val zoom = calculateZoomLevel(radiusMeters, minZoom ?: 1, maxZoom ?: 18)
        val bbox = calculateBoundingBox(lat, lon, radiusMeters)
        val tiles = getTilesInBoundingBox(bbox, zoom)

        val minX = tiles.minOf { it.x }
        val maxX = tiles.maxOf { it.x }
        val minY = tiles.minOf { it.y }
        val maxY = tiles.maxOf { it.y }

        val width = (maxX - minX + 1) * tileSize
        val height = (maxY - minY + 1) * tileSize

        val combinedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = combinedImage.createGraphics()
        log.info(tiles.size.toString())
        tiles.forEach { tileCoord ->
            val tileImage = fetchTileImage(tileCoord)
            g.drawImage(tileImage, (tileCoord.x - minX) * tileSize, (tileCoord.y - minY) * tileSize, null)
        }

        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(combinedImage, "png", outputStream)
        return outputStream.toByteArray()
    }

    private fun calculateZoomLevel(radiusMeters: Double, minZoom: Int, maxZoom: Int): Int {
        val zoom = (16 - log2(radiusMeters / 1000)).toInt()
        return zoom.coerceIn(minZoom, maxZoom)
    }

    private fun calculateBoundingBox(lat: Double, lon: Double, radiusMeters: Double): BoundingBox {
        val latRadians = Math.toRadians(lat)
        val lonRadians = Math.toRadians(lon)
        val earthRadius = 6378137.0 // in meters

        val latDelta = radiusMeters / earthRadius
        val lonDelta = asin(sin(radiusMeters / earthRadius) / cos(latRadians))

        val minLat = Math.toDegrees(latRadians - latDelta)
        val maxLat = Math.toDegrees(latRadians + latDelta)
        val minLon = Math.toDegrees(lonRadians - lonDelta)
        val maxLon = Math.toDegrees(lonRadians + lonDelta)

        return BoundingBox(minLat, minLon, maxLat, maxLon)
    }

    private fun getTilesInBoundingBox(bbox: BoundingBox, zoom: Int): List<TileCoordinate> {
        val minX = lon2tile(bbox.minLon, zoom)
        val maxX = lon2tile(bbox.maxLon, zoom)
        val minY = lat2tile(bbox.maxLat, zoom)
        val maxY = lat2tile(bbox.minLat, zoom)

        return (minX..maxX).flatMap { x ->
            (minY..maxY).map { y ->
                TileCoordinate(x, y, zoom)
            }
        }
    }

    private fun fetchTileImage(tileCoord: TileCoordinate): BufferedImage? {
        val url = "$tileServerUrl/${tileCoord.zoom}/${tileCoord.x}/${tileCoord.y}.png"

        val headers = org.springframework.http.HttpHeaders()
        headers.set("User-Agent", "MCP/1.0 (fachrial-dimas-putra.perdana@inovex.de)")

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
