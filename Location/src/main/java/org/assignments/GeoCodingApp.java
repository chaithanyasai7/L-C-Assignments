package org.assignments;

public class GeoCodingApp {

    public static void main(String[] args) {
        UserInputReader inputReader = new UserInputReader();
        GeoCodingService geoCodingService = new GeoCodingService(new GeoCodingHttpClient());
        ResultPrinter resultPrinter = new ResultPrinter();

        try {
            String placeName = inputReader.readPlaceName();
            GeoCoordinates coordinates = geoCodingService.getCoordinates(placeName);

            if (coordinates == null) {
                resultPrinter.printNotFound();
            } else {
                resultPrinter.printCoordinates(coordinates);
            }

        } catch (Exception e) {
            resultPrinter.printError(e.getMessage());
        }
    }
}