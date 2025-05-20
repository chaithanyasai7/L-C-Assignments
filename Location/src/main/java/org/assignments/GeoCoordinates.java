package org.assignments;

class GeoCoordinates {
    private final String latitude;
    private final String longitude;

    public GeoCoordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
