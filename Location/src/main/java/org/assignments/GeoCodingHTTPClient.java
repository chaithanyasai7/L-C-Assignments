package org.assignments;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

class GeoCodingHttpClient implements GeoCodingClient {

    @Override
    public String fetchLocationJson(String placeName) throws Exception {
        String encodedPlace = URLEncoder.encode(placeName, "UTF-8");
        String apiUrl = "https://nominatim.openstreetmap.org/search?q=" + encodedPlace + "&format=json";

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "GeocodeApp/1.0");

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder responseBuilder = new StringBuilder();
            String apiResponse;
            while ((apiResponse = bufferedReader.readLine()) != null) {
                responseBuilder.append(apiResponse);
            }
            return responseBuilder.toString();
        }
    }
}

