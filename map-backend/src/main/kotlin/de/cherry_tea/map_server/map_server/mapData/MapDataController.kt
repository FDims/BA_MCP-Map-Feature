package de.cherry_tea.map_server.map_server.mapData

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = ["http://localhost:3000"])
class MapController(private val mapDataService: MapDataService) {

    @GetMapping("/tiles/{z}/{x}/{y}.png")
    fun getTile(@PathVariable z: Int, @PathVariable x: Int, @PathVariable y: Int): ResponseEntity<ByteArray> {
        val tileData = mapDataService.generateTile(z, x, y)
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(tileData)
    }
}