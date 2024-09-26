package de.maptile.maptile_server

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@RestController
class TileController(private val tileService: TileService) {

    @GetMapping("/tile/{z}/{x}/{y}.png")
    fun getTile(@PathVariable z: Int, @PathVariable x: Int, @PathVariable y: Int): ResponseEntity<ByteArray> {
        val image = tileService.renderTile(z, x, y)

        if (image == null) {
            return ResponseEntity.noContent().build()
        }

        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(baos.toByteArray())
    }
}