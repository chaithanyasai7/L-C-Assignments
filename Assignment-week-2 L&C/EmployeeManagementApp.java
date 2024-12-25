import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementApp {

    public static void main(String[] args) {
        int employeeId1 = 1;
        String name1 = "Vadde Sai";
        String department1 = "SDET Automation";
        boolean isWorking1 = true;

        int employeeId2 = 2;
        String name2 = "Chaithanya";
        String department2 = "Manual Testing";
        boolean isWorking2 = true;

        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(employeeId1, name1, department1, isWorking1));
        employees.add(new Employee(employeeId2, name2, department2, isWorking2));

        String xmlReport = generateEmployeeReportXML(employees);
        System.out.println("XML Report: \n" + xmlReport);

        String csvReport = generateEmployeeReportCSV(employees);
        System.out.println("CSV Report: \n" + csvReport);

        saveEmployeeToDatabase(employees.get(0));
    }

    public static String generateEmployeeReportXML(List<Employee> employees) {
        StringBuilder xmlReport = new StringBuilder();
        xmlReport.append("<employees>\n");

        for (Employee employee : employees) {
            xmlReport.append("  <employee>\n")
                    .append("    <id>").append(employee.getEmployeeId()).append("</id>\n")
                    .append("    <name>").append(employee.getName()).append("</name>\n")
                    .append("    <department>").append(employee.getDepartment()).append("</department>\n")
                    .append("    <working>").append(employee.isWorking()).append("</working>\n")
                    .append("  </employee>\n");
        }

        xmlReport.append("</employees>");
        return xmlReport.toString();
    }

    public static String generateEmployeeReportCSV(List<Employee> employees) {
        StringBuilder csvReport = new StringBuilder();
        csvReport.append("EmployeeId,Name,Department,Working\n");

        for (Employee employee : employees) {
            csvReport.append(employee.getEmployeeId()).append(",")
                    .append(employee.getName()).append(",")
                    .append(employee.getDepartment()).append(",")
                    .append(employee.isWorking()).append("\n");
        }

        return csvReport.toString();
    }

    public static void saveEmployeeToDatabase(Employee employee) {
        System.out.println("Saving employee to database: " + employee.getEmployeeId());
    }
}

class Employee {
    private int employeeId;
    private String name;
    private String department;
    private boolean isWorking;

    public Employee(int employeeId, String name, String department, boolean isWorking) {
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.isWorking = isWorking;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isWorking() {
        return isWorking;
    }
}
