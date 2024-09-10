import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Circle, Popup, Marker, CircleMarker } from 'react-leaflet';
import { LatLngTuple, DivIcon } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';
import L from 'leaflet';

interface MapArea {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  radius: number;
  osmData: string;
}

interface POI {
  id: number;
  lat: number;
  lon: number;
  tags: {
    name?: string;
    amenity?: string;
  };
}

const App: React.FC = () => {
  const [mapArea, setMapArea] = useState<MapArea | null>(null);
  const [pois, setPois] = useState<POI[]>([]);
  const [input, setInput] = useState('');
  const [radius, setRadius] = useState(1000);
  const [error, setError] = useState('');

  const fetchMapArea = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/map/place", {
        params: { placeName: input, radius: radius }
      });
      console.log(response)
      setMapArea(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch map area');
      console.error(err);
    }
  };

  useEffect(() => {
    if (mapArea && mapArea.osmData) {
      try {
        const osmJson = JSON.parse(mapArea.osmData);
        const extractedPois = osmJson.elements.filter((element: any) =>
            element.type === 'node' && element.tags && (element.tags.name || element.tags.amenity)
        ).map((poi: any) => ({
          id: poi.id,
          lat: poi.lat,
          lon: poi.lon,
          tags: poi.tags
        }));
        setPois(extractedPois);
      } catch (err) {
        console.error('Failed to parse OSM data', err);
      }
    }
  }, [mapArea]);

  const customIcon = new L.DivIcon({
    className: 'custom-div-icon',
    html: "<div style='background-color:#c30b82;' class='marker-pin'></div>",
    iconSize: [30, 42],
    iconAnchor: [15, 42]
  });

  return (
      <div className="App">
        <h1>OpenStreetMap Area Viewer</h1>
        <div>
          <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Enter place name"
          />
          <input
              type="number"
              value={radius}
              onChange={(e) => setRadius(Number(e.target.value))}
              placeholder="Radius in meters"
          />
          <button onClick={fetchMapArea}>Fetch Map Area</button>
        </div>
        {error && <p className="error">{error}</p>}
        {mapArea && (
            <MapContainer
                center={[mapArea.latitude, mapArea.longitude] as LatLngTuple}
                zoom={13}
                style={{ height: '400px', width: '100%' }}
            >
              <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              />
              <Circle
                  center={[mapArea.latitude, mapArea.longitude] as LatLngTuple}
                  radius={mapArea.radius}
              >
                <Popup>{mapArea.name}</Popup>
              </Circle>
              {pois.map(poi => (
                  <Marker
                      key={poi.id}
                      position={[poi.lat, poi.lon] as LatLngTuple}
                      icon={customIcon}
                  >
                    <Popup>
                      {poi.tags.name || poi.tags.amenity || 'Point of Interest'}
                    </Popup>
                  </Marker>
              ))}
            </MapContainer>
        )}
      </div>
  );
};

export default App;