public class Main {
    public static void main(String[] args) {
        Wallet wallet = new Wallet(new Money(10.00f));
        Customer customer = new Customer("Chaithanya", "Sai", wallet);
        DeliveryBoy deliveryBoy = new DeliveryBoy();

        Money paymentAmount = new Money(2.00f);
        deliveryBoy.requestPayment(customer, paymentAmount);

        Money largePaymentAmount = new Money(15.00f);
        deliveryBoy.requestPayment(customer, largePaymentAmount);
    }
}
