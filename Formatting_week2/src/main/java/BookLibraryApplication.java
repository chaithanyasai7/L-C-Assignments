class Book {
    private String title;
    private String author;
    private int currentPage;

    // Constructor to initialize the title, author, and currentPage
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.currentPage = 0;
    }
    
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public void turnPage() {
        currentPage++;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}

class BookLocation {
    private String shelfNumber;
    private String roomNumber;

    // Constructor to initialize shelfNumber and roomNumber
    public BookLocation(String shelfNumber, String roomNumber) {
        this.shelfNumber = shelfNumber;
        this.roomNumber = roomNumber;
    }

    public String getLocation() {
        return "Shelf: " + shelfNumber + ", Room: " + roomNumber;
    }
}

class BookStorage {
    public void saveBook(Book book) {
        String filename = "/documents/" + book.getTitle() + " - " + book.getAuthor();
        System.out.println("Saving book to " + filename);
    }
}

interface Printer {
    // Method to print a page of the book
    void printPage(String pageContent);
}

class PlainTextPrinter implements Printer {
    // Print the page content as plain text
    @Override
    public void printPage(String pageContent) {
        System.out.println(pageContent);
    }
}

class HtmlPrinter implements Printer {
    // Print the page content in HTML format
    @Override
    public void printPage(String pageContent) {
        System.out.println("<div class='single-page'>" + pageContent + "</div>");
    }
}

class BookPrinter {
    private Printer printer;

    public BookPrinter(Printer printer) {
        this.printer = printer;
    }

    public void printBookPage(String pageContent) {
        printer.printPage(pageContent);
    }
}

public class BookLibraryApplication {
    public static void main(String[] args) {
        // Create a new book and turn some pages
        Book book = new Book("Atomic Habits", "James Clear");
        book.turnPage();
        book.turnPage();

        // Create a book location
        BookLocation location = new BookLocation("Shelf 3", "Room 2");

        BookStorage storage = new BookStorage();
        storage.saveBook(book);

        Printer plainTextPrinter = new PlainTextPrinter();
        BookPrinter bookPrinter = new BookPrinter(plainTextPrinter);
        bookPrinter.printBookPage("Content of page " + book.getCurrentPage());

        Printer htmlPrinter = new HtmlPrinter();
        bookPrinter = new BookPrinter(htmlPrinter);
        bookPrinter.printBookPage("Content of page " + book.getCurrentPage());

        System.out.println("Book is located at: " + location.getLocation());
    }
}
