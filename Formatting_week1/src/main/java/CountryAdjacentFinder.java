import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CountryAdjacentFinder {

    private static final Map<String, String[]> countryAdjacents = new HashMap<>();

    // Initialize adjacent countries for each country code
    static {
        countryAdjacents.put("IN", new String[]{"PK", "CN", "BT", "NP", "MM", "AF"});
        countryAdjacents.put("US", new String[]{"CA", "MX"});
        countryAdjacents.put("NZ", new String[]{"AU"});
        countryAdjacents.put("CN", new String[]{"AF", "BT", "IN", "KZ", "KG", "LA", "MN", "MM", "NP", "KP", "PK", "RU", "TJ", "VN"});
        countryAdjacents.put("PK", new String[]{"IN", "AF", "IR", "CN"});
        countryAdjacents.put("AF", new String[]{"IN", "PK", "TJ", "UZ"});
        countryAdjacents.put("MM", new String[]{"IN", "BD", "TH", "CN", "LA"});
        countryAdjacents.put("BD", new String[]{"IN", "MM"});
        countryAdjacents.put("RU", new String[]{"NO", "FI", "EE", "LV", "LT", "PL", "BY", "UA", "MD", "RO"});
        countryAdjacents.put("DE", new String[]{"DK", "PL", "CZ", "AT", "CH", "FR", "LU", "BE", "NL"});
        countryAdjacents.put("BR", new String[]{"AR", "BO", "CO", "GY", "PY", "PE", "SUR", "UY", "VE"});
    }

    public static void main(String[] args) {
        String inputCodes = getInputFromUser();

        String[] countryCodes = parseCountryCodes(inputCodes);

        for (String countryCode : countryCodes) {
            displayAdjacentCountries(countryCode);
        }
    }

    private static String getInputFromUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the country codes (e.g., IN, US, NZ): ");
        return scanner.nextLine().toUpperCase();
    }

    private static String[] parseCountryCodes(String inputCodes) {
        return inputCodes.split(",\\s*");
    }

    private static void displayAdjacentCountries(String countryCode) {
        String[] adjacentCountries = countryAdjacents.get(countryCode);
        System.out.println("Adjacent countries for " + countryCode + ":");

        if (adjacentCountries != null) {
            for (String code : adjacentCountries) {
                System.out.println(getCountryName(code));
            }
        } else {
            System.out.println("No adjacent countries found for the given country code.");
        }
        System.out.println();
    }

    private static String getCountryName(String countryCode) {
        switch (countryCode) {
            case "PK": return "Pakistan";
            case "CN": return "China";
            case "BT": return "Bhutan";
            case "NP": return "Nepal";
            case "CA": return "Canada";
            case "MX": return "Mexico";
            case "AU": return "Australia";
            case "AF": return "Afghanistan";
            case "MM": return "Myanmar";
            case "BD": return "Bangladesh";
            case "RU": return "Russia";
            case "DE": return "Germany";
            case "BR": return "Brazil";
            default: return "Unknown Country";
        }
    }
}
