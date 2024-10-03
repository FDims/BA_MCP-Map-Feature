package de.maptile.maptile_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@SpringBootApplication
class MaptileServerApplication

fun main(args: Array<String>) {
	runApplication<MaptileServerApplication>(*args)
}

@Configuration
class CorsConfig {
	@Bean
	fun corsFilter(): CorsFilter {
		val source = UrlBasedCorsConfigurationSource()
		val config = CorsConfiguration()
		config.allowCredentials = true
		config.addAllowedOrigin("http://localhost:63343") // Allow your frontend origin
		config.addAllowedHeader("*")
		config.addAllowedMethod("*")
		source.registerCorsConfiguration("/**", config)
		return CorsFilter(source)
	}
}