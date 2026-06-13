package  com.bank.exception;

public class InsufficientFundsException extends BankException{
    private final double requested;
    private final double available;
    
    public InsufficientFundsException(double requested,double available){
        super(String.format("Requested %.2f but only %.2f available", requested, available));
        this.requested = requested;
        this.available = available;
    }
    public double getRequested(){
        return requested;
    }
    public double getAvailable(){
        return available;
    }
}
