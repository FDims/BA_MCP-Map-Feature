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

    var resourceDIR = "../../../../../../resources/"
    fun renderTile(z: Int, x: Int, y: Int): ByteArray {
        val bbox = calculateBoundingBox(z, x, y)
        val mapnikCommand = "mapnik-render -m ${resourceDIR}mapnik-style.xml -o tile.png -b $bbox -w 256 -h 256"

        Runtime.getRuntime().exec(mapnikCommand).waitFor()

        return FileInputStream(File("tile.png")).readBytes()
    }

    private fun calculateBoundingBox(z: Int, x: Int, y: Int): String {
        val n = Math.pow(2.0, z.toDouble())
        val lonMin = x / n * 360.0 - 180.0
        val latMin = Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n))) * 180.0 / Math.PI
        val lonMax = (x + 1) / n * 360.0 - 180.0
        val latMax = Math.atan(Math.sinh(Math.PI * (1 - 2 * (y + 1) / n))) * 180.0 / Math.PI

        return "$lonMin,$latMin,$lonMax,$latMax"
    }

}