package de.cherry_tea.map_server.map_server.mapData

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = ["http://localhost:3000"])
class MapController(private val mapDataService: MapDataService) {

    @GetMapping("/coordinates")
    fun getMapByCoordinates(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam radius: Double,
        @RequestParam name: String
    ): MapDataEntity {
        return mapDataService.getMapArea(latitude, longitude, radius, name)
    }

    @GetMapping("/place")
    fun getMapByPlaceName(
        @RequestParam placeName: String,
        @RequestParam radius: Double
    ): MapDataEntity {
        return mapDataService.getMapAreaByName(placeName, radius)
    }
}