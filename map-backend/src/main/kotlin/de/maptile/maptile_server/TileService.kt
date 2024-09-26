package de.maptile.maptile_server


import org.geotools.map.MapContent
import org.geotools.map.Layer
import org.geotools.renderer.lite.StreamingRenderer
import org.geotools.data.simple.SimpleFeatureSource
import org.geotools.map.FeatureLayer
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.awt.Rectangle
import java.awt.RenderingHints
import org.geotools.geometry.jts.ReferencedEnvelope
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.springframework.beans.factory.annotation.Value
import org.geotools.data.DataStore
import org.geotools.data.DataStoreFinder
import org.geotools.styling.*
import java.awt.Color
import org.slf4j.LoggerFactory
import org.geotools.factory.CommonFactoryFinder
import org.locationtech.jts.geom.Envelope
import org.opengis.filter.FilterFactory2
import javax.annotation.PostConstruct
import org.geotools.referencing.CRS
import org.geotools.geometry.jts.JTS
import org.opengis.referencing.crs.CoordinateReferenceSystem

@Service
class TileService(
    @Value("\${db.host}") private val dbHost: String,
    @Value("\${db.port}") private val dbPort: Int,
    @Value("\${db.name}") private val dbName: String,
    @Value("\${db.user}") private val dbUser: String,
    @Value("\${db.password}") private val dbPassword: String
) {
    private val logger = LoggerFactory.getLogger(TileService::class.java)
    private lateinit var dataStore: DataStore
    private lateinit var mapContent: MapContent
    private lateinit var styleFactory: StyleFactory
    private lateinit var filterFactory: FilterFactory2
    private lateinit var crs3857: CoordinateReferenceSystem


    @PostConstruct
    fun init() {
        crs3857 = CRS.decode("EPSG:3857")
        styleFactory = CommonFactoryFinder.getStyleFactory()
        filterFactory = CommonFactoryFinder.getFilterFactory2()
        dataStore = createDataStore()
        mapContent = createMapContent()
    }


    private fun createDataStore(): DataStore {
        val params = mapOf(
            "dbtype" to "postgis",
            "host" to dbHost,
            "port" to dbPort.toString(),
            "database" to dbName,
            "user" to dbUser,
            "passwd" to dbPassword
        )
        return DataStoreFinder.getDataStore(params)
            ?: throw IllegalStateException("Could not connect to the database")
    }

    private fun createMapContent(): MapContent {
        val mapContent = MapContent()
        try {
            mapContent.addLayer(createLayer("planet_osm_point", createPointStyle()))
            mapContent.addLayer(createLayer("planet_osm_line", createLineStyle()))
            mapContent.addLayer(createLayer("planet_osm_polygon", createPolygonStyle()))
            mapContent.addLayer(createLayer("planet_osm_roads", createLineStyle()))
        } catch (e: Exception) {
            logger.error("Error creating map content", e)
            throw IllegalStateException("Failed to create map content", e)
        }
        return mapContent
    }

    private fun createLayer(tableName: String, style: Style): Layer {
        val featureSource = dataStore.getFeatureSource(tableName) as SimpleFeatureSource
        return FeatureLayer(featureSource, style)
    }

    private fun createPointStyle(): Style {
        val mark = styleFactory.circleMark ?: styleFactory.createMark()
        mark.fill.color = filterFactory.literal(Color.RED)
        mark.stroke.color = filterFactory.literal(Color.BLACK)
        mark.stroke.width = filterFactory.literal(1)

        val graphic = styleFactory.createGraphic(
            null, // ExternalGraphics
            arrayOf(mark), // Marks
            null, // Symbols
            filterFactory.literal(1), // Opacity
            filterFactory.literal(5), // Size
            filterFactory.literal(0) // Rotation
        )

        val symbolizer = styleFactory.createPointSymbolizer(graphic, null)
        val rule = styleFactory.createRule()
        rule.symbolizers().add(symbolizer)
        val featureTypeStyle = styleFactory.createFeatureTypeStyle(rule)
        val style = styleFactory.createStyle()
        style.featureTypeStyles().add(featureTypeStyle)

        return style
    }

    private fun createLineStyle(): Style {
        val strokeColor = filterFactory.literal(Color.BLUE)
        val strokeWidth = filterFactory.literal(1)
        val symbolizer = styleFactory.createLineSymbolizer(
            styleFactory.createStroke(strokeColor, strokeWidth),
            null
        )
        val rule = styleFactory.createRule()
        rule.symbolizers().add(symbolizer)
        val featureTypeStyle = styleFactory.createFeatureTypeStyle(rule)
        val style = styleFactory.createStyle()
        style.featureTypeStyles().add(featureTypeStyle)
        return style
    }

    private fun createPolygonStyle(): Style {
        val fillColor = filterFactory.literal(Color.LIGHT_GRAY)
        val strokeColor = filterFactory.literal(Color.DARK_GRAY)
        val strokeWidth = filterFactory.literal(0.5)
        val symbolizer = styleFactory.createPolygonSymbolizer(
            styleFactory.createStroke(strokeColor, strokeWidth),
            styleFactory.createFill(fillColor),
            null
        )
        val rule = styleFactory.createRule()
        rule.symbolizers().add(symbolizer)
        val featureTypeStyle = styleFactory.createFeatureTypeStyle(rule)
        val style = styleFactory.createStyle()
        style.featureTypeStyles().add(featureTypeStyle)
        return style
    }

    fun renderTile(z: Int, x: Int, y: Int): BufferedImage? {
        val tileSize = 256
        val worldWidth = 20037508.34 * 2
        val worldHeight = 20037508.34 * 2

        val zoomFactor = Math.pow(2.0, z.toDouble())
        val tileWidth = worldWidth / zoomFactor
        val tileHeight = worldHeight / zoomFactor

        val minX = x * tileWidth - 20037508.34
        val minY = 20037508.34 - (y + 1) * tileHeight
        val maxX = (x + 1) * tileWidth - 20037508.34
        val maxY = 20037508.34 - y * tileHeight

        val germanyBBox = ReferencedEnvelope(
            5.8663, 15.0419, 47.2701, 55.0584, DefaultGeographicCRS.WGS84
        )

        // Transform Germany's BBox to EPSG:3857
        val transformedGermanyBBox = JTS.transform(germanyBBox, CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs3857))

        val tileBBox = ReferencedEnvelope(minX, maxX, minY, maxY, crs3857)

        if (!transformedGermanyBBox.intersects(tileBBox as Envelope)) {
            return null
        }

        val mapBounds = ReferencedEnvelope(minX, maxX, minY, maxY, crs3857)

        val image = BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        val renderer = StreamingRenderer()
        renderer.mapContent = mapContent
        renderer.java2DHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        renderer.paint(graphics, Rectangle(tileSize, tileSize), mapBounds)
        graphics.dispose()

        return image
    }
}
