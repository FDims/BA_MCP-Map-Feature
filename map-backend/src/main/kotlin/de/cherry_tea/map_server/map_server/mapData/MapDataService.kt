package de.cherry_tea.map_server.map_server.mapData

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.net.URLEncoder

@Service
class MapDataService(private val mapDataRepository: MapDataRepository) {

    private val restTemplate = RestTemplate()
    private val overpassApiUrl = "https://overpass-api.de/api/interpreter"

    fun getMapArea (latitude: Double, longitude:Double, radius:Double, name:String): MapDataEntity {
        val query = """
            [out:json];
            (
            node(around:${radius},${latitude},${longitude});
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()

        val response = restTemplate.postForObject(
            overpassApiUrl,
            "data=${URLEncoder.encode(query, "UTF-8")}",
            String::class.java
        )

        val mapData = MapDataEntity(
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            osmData = response?: ""
        )

        return mapDataRepository.save(mapData)
    }

    fun getMapAreaByName(name: String, radius: Double): MapDataEntity {
        val (latitude, longitude)= getCoordinatesFromName(name)
        return getMapArea(latitude,longitude,radius,name)
    }

    private fun getCoordinatesFromName(name: String): Pair<Double, Double>{
        val url = "https://nominatim.openstreetmap.org/search?q=${name}&format=json"
        val response: Array<Map<String, Any>>? = restTemplate.getForObject(url)
        var latitude: Double = 0.0
        var longitude: Double = 0.0

        if (response != null) {
            response.firstOrNull()?.let{
                latitude = it["lat"]?.toString()?.toDouble()?:0.0
                longitude = it["lon"]?.toString()?.toDouble()?:0.0
            }
        }

        return Pair(latitude,longitude)
    }

}