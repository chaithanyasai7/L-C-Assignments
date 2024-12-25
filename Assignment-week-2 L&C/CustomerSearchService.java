import java.util.ArrayList;
import java.util.List;

class Customer {
    int id;
    String companyName;
    String contactName;
    String country;

    public Customer(int id, String companyName, String contactName, String country) {
        this.id = id;
        this.companyName = companyName;
        this.contactName = contactName;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getCountry() {
        return country;
    }
}

public class CustomerSearchService {

    private List<Customer> customerDatabase = new ArrayList<>();

    public CustomerSearchService() {
        customerDatabase.add(new Customer(1, "Company A", "Vadde Chaithanya", "USA"));
        customerDatabase.add(new Customer(2, "Company B", "Chaithanya", "Canada"));
        customerDatabase.add(new Customer(3, "Company C", "sai vadde", "USA"));
        customerDatabase.add(new Customer(4, "Company D", "vadde sai", "UK"));
    }

    public List<Customer> searchCustomersByCountry(String country) {
        List<Customer> matchingCustomers = new ArrayList<>();
        for (Customer customer : customerDatabase) {
            if (customer.getCountry().contains(country)) {
                matchingCustomers.add(customer);
            }
        }
        return matchingCustomers;
    }

    public List<Customer> searchCustomersByCompanyName(String companyName) {
        List<Customer> matchingCustomers = new ArrayList<>();
        for (Customer customer : customerDatabase) {
            if (customer.getCompanyName().contains(companyName)) {
                matchingCustomers.add(customer);
            }
        }
        return matchingCustomers;
    }

    public List<Customer> searchCustomersByContactName(String contactName) {
        List<Customer> matchingCustomers = new ArrayList<>();
        for (Customer customer : customerDatabase) {
            if (customer.getContactName().contains(contactName)) {
                matchingCustomers.add(customer);
            }
        }
        return matchingCustomers;
    }

    public String exportCustomersToCSV(List<Customer> customers) {
        StringBuilder csvData = new StringBuilder();
        for (Customer customer : customers) {
            csvData.append(customer.getId()).append(",")
                    .append(customer.getCompanyName()).append(",")
                    .append(customer.getContactName()).append(",")
                    .append(customer.getCountry()).append("\n");
        }
        return csvData.toString();
    }

    public static void main(String[] args) {
        CustomerSearchService searchService = new CustomerSearchService();

        List<Customer> customersFromUSA = searchService.searchCustomersByCountry("USA");
        System.out.println("Customers from USA:");
        for (Customer customer : customersFromUSA) {
            System.out.println(customer.getCompanyName());
        }

        List<Customer> contactSam = searchService.searchCustomersByContactName("Sam Johnson");
        System.out.println("\nCustomer with Contact Name 'Sam Johnson':");
        for (Customer customer : contactSam) {
            System.out.println(customer.getCompanyName());
        }

        List<Customer> companyAClients = searchService.searchCustomersByCompanyName("Company A");
        System.out.println("\nCustomers from Company A:");
        for (Customer customer : companyAClients) {
            System.out.println(customer.getCompanyName());
        }

        String csvOutput = searchService.exportCustomersToCSV(customersFromUSA);
        System.out.println("\nExported CSV Data:\n" + csvOutput);
    }
}
