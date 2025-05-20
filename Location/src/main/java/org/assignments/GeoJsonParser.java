package org.assignments;

class GeoJsonParser {

    public static GeoCoordinates parseCoordinates(String json) {
        String latitude = extractValue(json, "\"lat\":\"", "\"");
        String longitude = extractValue(json, "\"lon\":\"", "\"");

        if (latitude == null || longitude == null) {
            return null;
        }
        return new GeoCoordinates(latitude, longitude);
    }

    private static String extractValue(String json, String keyStart, String keyEnd) {
        int start = json.indexOf(keyStart);
        if (start == -1) return null;
        start += keyStart.length();
        int end = json.indexOf(keyEnd, start);
        if (end == -1) return null;
        return json.substring(start, end);
    }
}
