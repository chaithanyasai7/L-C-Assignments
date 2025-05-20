package org.assignments;

class ResultPrinter {

    public void printCoordinates(GeoCoordinates coordinates) {
        System.out.println("Latitude: " + coordinates.getLatitude());
        System.out.println("Longitude: " + coordinates.getLongitude());
    }

    public void printNotFound() {
        System.out.println("Could not find location.");
    }

    public void printError(String message) {
        System.out.println("Error: " + message);
    }
}
