import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TumblrAPI {

    public static void main(String[] args) {
        try {
            // Get user input for blog name and post range
            String blogName = getUserInput("Enter blog name (e.g., 'good' for good.tumblr.com): ");
            String postRange = getUserInput("Enter post range (start-end, e.g., '88-100'): ");

            String[] postRangeArray = postRange.split("-");
            int startPost = 0, endPost = 0;

            // Handle potential NumberFormatException when parsing the post range
            try {
                startPost = Integer.parseInt(postRangeArray[0]);
                endPost = Integer.parseInt(postRangeArray[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid post range format. Please enter integers for the post range.");
                return;
            }

            // Fetch and display blog and image data
            fetchAndDisplayBlogData(blogName, startPost, endPost);

        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get user input from the console
    private static String getUserInput(String prompt) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print(prompt);
        return scanner.nextLine();
    }

    // Method to fetch and display the blog and image data
    private static void fetchAndDisplayBlogData(String blogName, int startPost, int endPost) {
        try {
            String apiUrl = constructApiUrl(blogName, startPost, endPost);
            String jsonResponse = sendApiRequest(apiUrl);

            // Print raw response for debugging
            System.out.println("Raw Response: " + jsonResponse);

            // Parse JSON response
            JSONObject jsonObject = parseJsonResponse(jsonResponse);
            printBlogInfo(jsonObject);
            printImageUrls(jsonObject, startPost, endPost);

        } catch (Exception e) {
            System.out.println("Error occurred while fetching blog data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to construct the API URL
    private static String constructApiUrl(String blogName, int startPost, int endPost) {
        return "https://" + blogName + ".tumblr.com/api/read/json?type=photo&num=" + (endPost - startPost + 1) + "&start=" + startPost;
    }

    // Method to send the API request and get the response as a string
    private static String sendApiRequest(String apiUrl) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    // Method to parse the JSON response
    private static JSONObject parseJsonResponse(String jsonResponse) throws JSONException {
        String jsonResponseCleaned = jsonResponse.replaceFirst("var tumblr_api_read = ", "");
        jsonResponseCleaned = jsonResponseCleaned.substring(0, jsonResponseCleaned.length() - 1);
        return new JSONObject(jsonResponseCleaned);
    }

    // Method to print basic blog information (title, description, name, number of posts)
    private static void printBlogInfo(JSONObject jsonObject) throws JSONException {
        try {
            JSONObject tumblelog = jsonObject.getJSONObject("tumblelog");
            String title = tumblelog.getString("title");
            String description = tumblelog.getString("description");
            String name = tumblelog.getString("name");
            int numberOfPosts = jsonObject.getInt("posts-total");

            System.out.println("\nBlog Info:");
            System.out.println("Title: " + title);
            System.out.println("Description: " + description);
            System.out.println("Name: " + name);
            System.out.println("Number of Posts: " + numberOfPosts);
        } catch (JSONException e) {
            System.out.println("Error occurred while extracting blog info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to print image URLs for the specified post range
    private static void printImageUrls(JSONObject jsonObject, int startPost, int endPost) {
        try {
            JSONArray posts = jsonObject.getJSONArray("posts");

            for (int i = startPost - 1; i < endPost && i < posts.length(); i++) {
                JSONObject post = posts.getJSONObject(i);

                if (post.has("photos")) {
                    JSONArray photos = post.getJSONArray("photos");

                    for (int j = 0; j < photos.length(); j++) {
                        JSONObject photo = photos.getJSONObject(j);
                        String imageUrl = photo.getString("photo-url-1280");

                        System.out.println("Post " + (i + 1) + ". " + imageUrl);
                    }
                } else {
                    System.out.println("Post " + (i + 1) + " does not contain any images.");
                }
            }
        } catch (JSONException e) {
            System.out.println("Error occurred while extracting image URLs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}