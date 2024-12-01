import { GoogleMap } from '@capacitor/google-maps';
import { memo, useCallback, useEffect, useRef } from 'react';
import { getLogger } from '../core';

interface MyMapProps {
    latitude: number;
    longitude: number;
    onMapClick: (e: any) => void;
    onMarkerClick: (e: any) => void;
    mode: 'addMarker' | 'viewOnly';
    games?: { location: { latitude: number, longitude: number }, title: string }[];
}

const log = getLogger("MyMap");

const MyMap: React.FC<MyMapProps> = ({ latitude, longitude, onMapClick, onMarkerClick, mode, games }) => {
    const mapRef = useRef<HTMLElement>(null);
    const googleMapRef = useRef<GoogleMap | null>(null);
    const markerIdRef = useRef<string | null>(null);

    useEffect(() => {
        let mounted = true;

        createMap();

        return () => {
            mounted = false;
            googleMapRef.current?.removeAllMapListeners();
        };

        async function createMap() {
            if (!mounted || !mapRef.current) {
                return;
            }

            googleMapRef.current = await GoogleMap.create({
                id: "my-game-map",
                element: mapRef.current,
                apiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
                config: {
                    center: { lat: latitude, lng: longitude },
                    zoom: 12,
                }
            });

            log("Map created");

            // Add markers for each game
            if (games) {
                games.forEach(async game => {
                    await googleMapRef.current?.addMarker({
                        coordinate: { lat: game.location.latitude, lng: game.location.longitude },
                        title: game.title
                    });
                });
            }

            if (mode === 'addMarker') {
                markerIdRef.current = await googleMapRef.current.addMarker({ coordinate: { lat: latitude, lng: longitude }, title: 'My Location' });
            }

            // Map click: add a new marker on the specified position
            // Works only in the context of add/edit an entity
            // Modifies the position of the entity
            await googleMapRef.current.setOnMapClickListener(async ({ latitude, longitude }) => {
                if (mode === 'addMarker') {
                    if (markerIdRef.current) {
                        await googleMapRef.current?.removeMarker(markerIdRef.current).catch(() => {
                            log("Marker not found");
                        });
                        markerIdRef.current = null;
                    }
                    markerIdRef.current = await googleMapRef.current?.addMarker({
                        coordinate: { lat: latitude, lng: longitude },
                        title: 'New Location'
                    }) ?? null;
                    onMapClick({ latitude, longitude });
                }
            });

            // Marker click: identify an entity by its location
            // Works only in the context of viewing the entities
            await googleMapRef.current.setOnMarkerClickListener(({ markerId, latitude, longitude }) => {
                if (mode === 'viewOnly') {
                    onMarkerClick({ markerId, latitude, longitude });
                }
            });
        }


    }, [mode, games]);

    useEffect(() => {
        let mounted = true;

        updateMap();

        return () => {
            mounted = false;
        };

        // Updates the map position and marker position
        async function updateMap() {
            if (mounted && googleMapRef.current) {
                googleMapRef.current.setCamera({
                    coordinate: { lat: latitude, lng: longitude },
                    zoom: 12,
                    animate: true
                });

                if (mounted && markerIdRef.current && mode === 'addMarker') {
                    googleMapRef.current.removeMarker(markerIdRef.current).catch(() => {
                        log("Marker not found");
                        markerIdRef.current = null;
                    });
                    markerIdRef.current = await googleMapRef.current.addMarker({
                        coordinate: { lat: latitude, lng: longitude },
                        title: 'My Location'
                    });
                }
            }
        }
    }, [latitude, longitude, mode]);

    return (
        <div className='map-container'>
            <capacitor-google-map ref={mapRef} style={{
                display: "block",
                width: "100%",
                height: "400px",
            }} />
        </div>
    )
}

export default memo(MyMap);