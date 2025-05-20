package org.assignments;

class GeoCodingService {

    private final GeoCodingClient geoCodingClient;

    public GeoCodingService(GeoCodingClient geoCodingClient) {
        this.geoCodingClient = geoCodingClient;
    }

    public GeoCoordinates getCoordinates(String placeName) throws Exception {
        String jsonResponse = geoCodingClient.fetchLocationJson(placeName);
        return GeoJsonParser.parseCoordinates(jsonResponse);
    }
}
