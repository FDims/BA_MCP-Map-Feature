package de.maptile.maptile_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MaptileServerApplication

fun main(args: Array<String>) {
	runApplication<MaptileServerApplication>(*args)
}
