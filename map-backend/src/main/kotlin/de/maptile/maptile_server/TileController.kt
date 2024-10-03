package de.maptile.maptile_server

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class MapController(private val mapService: MapService) {
    @GetMapping("/Welcome")
    fun weclomeFunc(): String{
        return "Welcome, The Server is Working!"
    }
    @GetMapping("/map-data")
    fun getMapImage(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam radiusMeters: Double,
        @RequestParam(required = false) minZoom: Int?,
        @RequestParam(required = false) maxZoom: Int?
    ): ResponseEntity<ByteArray> {
        val imageBytes = mapService.getMapImageForArea(lat, lon, radiusMeters, minZoom, maxZoom)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes)
    }
}