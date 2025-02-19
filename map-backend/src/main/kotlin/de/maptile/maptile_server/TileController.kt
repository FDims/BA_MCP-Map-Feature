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
    @GetMapping("/map-data/bycoordinate")
    fun getMapImage(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam radiusMeters: Double,
    ): ResponseEntity<ByteArray> {
        val imageBytes = mapService.getSitePlanImageForArea(lat, lon, radiusMeters)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes)
    }

    @GetMapping("/map-data/byname")
    fun getMapImage(
        @RequestParam placeName : String,
        @RequestParam radiusMeters: Double,
    ): ResponseEntity<ByteArray> {
        val imageBytes = mapService.getSitePlanImageForAreaByName(placeName, radiusMeters)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes)
    }
}