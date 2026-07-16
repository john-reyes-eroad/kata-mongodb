# API Endpoints

## Vehicles

| Method | Endpoint | Functional | Notes |
|---|---|---|---|
| GET | `/api/vehicles` | Yes | Returns all vehicles. |
| GET | `/api/vehicles/search?keyword={keyword}` | Yes | Searches vehicles by VIN, make, or model. |
| GET | `/api/vehicles/count?keyword={keyword}` | Yes | Returns only the count; matches VIN, make, or model when a keyword is supplied. |
| GET | `/api/vehicles/{id}` | Yes | Returns one vehicle by ID; `404` if not found. |
| POST | `/api/vehicles` | Yes | Creates a vehicle; `201 Created` on success, `409 Conflict` on duplicate VIN. |
| PUT | `/api/vehicles/{id}` | Yes | Updates a vehicle; `404` if not found, `409 Conflict` on duplicate VIN. |
| DELETE | `/api/vehicles/{id}` | Yes | Deletes a vehicle; `204 No Content` on success. |

## Drivers

| Method | Endpoint | Functional | Notes |
|---|---|---|---|
| GET | `/api/drivers` | Yes | Returns all drivers. |
| GET | `/api/drivers/search?keyword={keyword}` | Yes | Searches drivers by name or license number. |
| GET | `/api/drivers/count?keyword={keyword}` | Yes | Returns only the count; matches name or license number when a keyword is supplied. |
| GET | `/api/drivers/{id}` | Yes | Returns one driver by ID; `404` if not found. |
| POST | `/api/drivers` | Yes | Creates a driver; `201 Created` on success, `409 Conflict` on duplicate name or license number. |
| PUT | `/api/drivers/{id}` | Yes | Updates a driver; `404` if not found, `409 Conflict` on duplicate name or license number. |
| DELETE | `/api/drivers/{id}` | Yes | Deletes a driver; `204 No Content` on success. |

## Trips

| Method | Endpoint | Functional | Notes |
|---|---|---|---|
| GET | `/api/trips` | Yes | Returns all trips. |
| GET | `/api/trips/count?keyword={keyword}` | Yes | Returns only the count; keyword matches a trip, vehicle, or driver ID. |
| GET | `/api/trips/{id}` | Yes | Returns one trip by ID; `404` if not found. |
| POST | `/api/trips` | Yes | Creates a trip using `vehicleId` and `driverId`; `201 Created` on success. |
| PUT | `/api/trips/{id}` | Yes | Updates a trip; `404` if not found. |
| DELETE | `/api/trips/{id}` | Yes | Deletes a trip; `204 No Content` on success. |

## Locations

| Method | Endpoint | Functional | Notes |
|---|---|---|---|
| GET | `/api/locations` | Yes | Returns all trip locations. |
| GET | `/api/locations/count?keyword={keyword}` | Yes | Returns only the count; keyword matches a location or trip ID. |
| GET | `/api/locations/{id}` | Yes | Returns one location by ID; `404` if not found. |
| POST | `/api/locations` | Yes | Creates a location using `tripId`; `201 Created` on success. |
| PUT | `/api/locations/{id}` | Yes | Updates a location; `404` if not found. |
| DELETE | `/api/locations/{id}` | Yes | Deletes a location; `204 No Content` on success. |

## Diagnostic Events

| Method | Endpoint | Functional | Notes |
|---|---|---|---|
| GET | `/api/diagnostic-events` | Yes | Returns all diagnostic events. |
| GET | `/api/diagnostic-events/count?keyword={keyword}` | Yes | Returns only the count; matches code, severity, description, event ID, or vehicle ID. |
| GET | `/api/diagnostic-events/{id}` | Yes | Returns one diagnostic event by ID; `404` if not found. |
| POST | `/api/diagnostic-events` | Yes | Creates a diagnostic event using `vehicleId`; `201 Created` on success. |
| PUT | `/api/diagnostic-events/{id}` | Yes | Updates a diagnostic event; `404` if not found. |
| DELETE | `/api/diagnostic-events/{id}` | Yes | Deletes a diagnostic event; `204 No Content` on success. |

## Actuator

| Method | Endpoint | Functional | Notes |
|---|---|---|---|
| GET | `/actuator/health` | Yes | Exposed by management config. |
| GET | `/actuator/info` | Yes | Exposed by management config. |
| GET | `/actuator/metrics` | Yes | Exposed by management config. |
