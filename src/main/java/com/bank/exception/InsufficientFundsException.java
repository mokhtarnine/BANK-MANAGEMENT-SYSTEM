package  com.bank.exception;

public class InsufficientFundsException extends BankException{
    private final double requested;
    private final double available;
    
    public InsufficientFundsException(double requested,double available){
        super("Requested" + requested + "but only "+ available+"available");
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