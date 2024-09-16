package de.cherry_tea.map_server.map_server.mapData

import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.WKBReader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp

@Service
class MapDataService(private val jdbcTemplate: JdbcTemplate) {
    private val tileSize = 256
    private val geometryFactory = GeometryFactory()
    private val wkbReader = WKBReader(geometryFactory)

    fun generateTile(z: Int, x: Int, y: Int): ByteArray {
        val envelope = calculateBoundingBox(z, x, y)
        val features = fetchFeatures(envelope)

        val image = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, tileSize, tileSize)

        drawFeatures(graphics, features, envelope)

        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }

    private fun calculateBoundingBox(z: Int, x: Int, y: Int): Envelope {
        val n = 1 shl z
        val lonMin = x / n.toDouble() * 360.0 - 180.0
        val latMin = atan(sinh(PI * (1 - 2 * y / n.toDouble()))).toDegrees()
        val lonMax = (x + 1) / n.toDouble() * 360.0 - 180.0
        val latMax = atan(sinh(PI * (1 - 2 * (y + 1) / n.toDouble()))).toDegrees()
        return Envelope(lonMin, lonMax, latMin, latMax)
    }

    private fun fetchFeatures(envelope: Envelope): List<FeatureEntity> {
        val sql = """
            SELECT ST_AsBinary(geometry) as geom, type
            FROM features
            WHERE ST_Intersects(geometry, ST_MakeEnvelope(?, ?, ?, ?, 4326))
        """
        return jdbcTemplate.query(sql, { rs, _ ->
            FeatureEntity(
                wkbReader.read(rs.getBytes("geom")),
                rs.getString("type")
            )
        }, envelope.minX, envelope.minY, envelope.maxX, envelope.maxY)
    }

    private fun drawFeatures(graphics: Graphics2D, features: List<FeatureEntity>, envelope: Envelope) {
        features.forEach { feature ->
            when (feature.type) {
                "building" -> graphics.color = Color.GRAY
                "park" -> graphics.color = Color.GREEN
                "stadium" -> graphics.color = Color.RED
                else -> graphics.color = Color.BLACK
            }
            graphics.stroke = BasicStroke(1f)

            val coordinates = feature.geometry.coordinates
            val pixelCoords = coordinates.map { coord ->
                val x = ((coord.x - envelope.minX) / (envelope.maxX - envelope.minX) * tileSize).toInt()
                val y = ((envelope.maxY - coord.y) / (envelope.maxY - envelope.minY) * tileSize).toInt()
                Pair(x, y)
            }

            val path = java.awt.geom.Path2D.Double()
            pixelCoords.forEachIndexed { index, (x, y) ->
                if (index == 0) path.moveTo(x.toDouble(), y.toDouble())
                else path.lineTo(x.toDouble(), y.toDouble())
            }
            path.closePath()

            graphics.fill(path)
            graphics.draw(path)
        }
    }
    fun Double.toDegrees() = this * 180.0 / PI
    fun sinh(x: Double) = (exp(x) - exp(-x)) / 2

}