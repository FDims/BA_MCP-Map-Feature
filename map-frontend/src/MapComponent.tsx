import React, { useState, useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

interface TileData {
    x: number;
    y: number;
    zoom: number;
    imageData: string;
}

interface MapData {
    [zoom: number]: TileData[];
}

interface MapComponentProps {
    lat: number;
    lon: number;
    minZoom: number;
    maxZoom: number;
}

const MapComponent: React.FC<MapComponentProps> = ({ lat, lon, minZoom, maxZoom }) => {
    const [mapData, setMapData] = useState<MapData>({});
    const [currentZoom, setCurrentZoom] = useState<number>(minZoom);
    const mapRef = useRef<L.Map | null>(null);
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const customLayerGroup = useRef<L.LayerGroup | null>(null);


    useEffect(() => {
        if (mapContainerRef.current && !mapRef.current) {
            mapRef.current = L.map(mapContainerRef.current, {
                center: [lat, lon],
                zoom: minZoom,
                minZoom: minZoom,
                maxZoom: maxZoom,
                zoomControl: true,
                attributionControl: false
            });

            customLayerGroup.current = L.layerGroup().addTo(mapRef.current);

            mapRef.current.on('zoomend', () => {
                if (mapRef.current) {
                    setCurrentZoom(mapRef.current.getZoom());
                }
            });
        }

        return () => {
            if (mapRef.current) {
                mapRef.current.remove();
                mapRef.current = null;
            }
        };
    }, [lat, lon, minZoom, maxZoom]);

    useEffect(() => {
        setError(null); // Reset error state before new fetch
        fetch(`http://localhost:8080/map-data?lat=${lat}&lon=${lon}&radiusMeters=10000&minZoom=${minZoom}&maxZoom=${maxZoom}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then((data: MapData) => setMapData(data))
            .catch(error => {
                console.error('Error fetching map data:', error);
                setError('Failed to load map data. Please try again later.');
            });
    }, [lat, lon, minZoom, maxZoom]);

    useEffect(() => {
        fetch(`http://localhost:8080/map-data?lat=${lat}&lon=${lon}&radiusMeters=10000&minZoom=${minZoom}&maxZoom=${maxZoom}`)
            .then(response => response.json())
            .then((data: MapData) => setMapData(data))
            .catch(error => console.error('Error fetching map data:', error));
    }, [lat, lon, minZoom, maxZoom]);

    useEffect(() => {
        if (mapRef.current && customLayerGroup.current && mapData[currentZoom]) {
            customLayerGroup.current.clearLayers();

            mapData[currentZoom].forEach((tile: TileData) => {
                const imgUrl = `data:image/png;base64,${tile.imageData}`;
                const bounds = L.latLngBounds(
                    [tile2lat(tile.y + 1, tile.zoom), tile2lon(tile.x, tile.zoom)],
                    [tile2lat(tile.y, tile.zoom), tile2lon(tile.x + 1, tile.zoom)]
                );
                L.imageOverlay(imgUrl, bounds).addTo(customLayerGroup.current!);
            });
        }
    }, [mapData, currentZoom]);

    return <div ref={mapContainerRef} style={{ height: '400px', width: '100%' }} />;
};

const tile2lon = (x: number, z: number): number => {
    return (x / Math.pow(2, z) * 360 - 180);
};

const tile2lat = (y: number, z: number): number => {
    const n = Math.PI - 2 * Math.PI * y / Math.pow(2, z);
    return (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
};

export default MapComponent;