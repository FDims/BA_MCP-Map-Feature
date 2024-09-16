package de.cherry_tea.map_server.map_server.mapData

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tiles")
class TileController(@Autowired private val tileService: TileService) {

    @GetMapping("/{z}/{x}/{y}.png", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getTile(@PathVariable z: Int, @PathVariable x: Int, @PathVariable y: Int): ResponseEntity<ByteArray> {
        val tileData = tileService.renderTile(z, x, y)
        return ResponseEntity.ok(tileData)
    }
}