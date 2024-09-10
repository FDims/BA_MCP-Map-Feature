package de.cherry_tea.map_server.map_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MapServerApplication

fun main(args: Array<String>) {
	runApplication<MapServerApplication>(*args)
}
