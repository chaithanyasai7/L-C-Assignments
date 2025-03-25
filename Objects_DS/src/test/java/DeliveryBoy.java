public class DeliveryBoy {
    public void requestPayment(Customer customer, Money paymentAmount) {
        if (customer.processPayment(paymentAmount)) {
            System.out.println("Payment successful! Thank you.");
        } else {
            System.out.println("Insufficient funds. Please try again later.");
        }
    }
}