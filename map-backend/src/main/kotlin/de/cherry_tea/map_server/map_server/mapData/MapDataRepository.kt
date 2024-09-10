package de.cherry_tea.map_server.map_server.mapData

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MapDataRepository : JpaRepository<MapDataEntity, Long>