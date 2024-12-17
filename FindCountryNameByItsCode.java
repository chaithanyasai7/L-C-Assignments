import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FindCountryNameByItsCode {
    public static void main(String[] args) {
        Map<String, String> countryMap = new HashMap<>();
        countryMap.put("AF", "Afghanistan");
        countryMap.put("AL", "Albania");
        countryMap.put("DZ", "Algeria");
        countryMap.put("AD", "Andorra");
        countryMap.put("AO", "Angola");
        countryMap.put("AR", "Argentina");
        countryMap.put("AU", "Australia");
        countryMap.put("BR", "Brazil");
        countryMap.put("CA", "Canada");
        countryMap.put("CN", "China");
        countryMap.put("IN", "India");
        countryMap.put("NZ", "New Zealand");
        countryMap.put("ZM","Zimbabwe");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the country code: ");
        String countryCodeInput = scanner.nextLine().toUpperCase();

        String countryName = countryMap.get(countryCodeInput);

        if (countryName != null) {
            System.out.println("Country name is: " + countryName);
        } else {
            System.out.println("Country code is incorrect");
        }
    }
}