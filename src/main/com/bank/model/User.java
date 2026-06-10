package  com.bank.model;

public abstract class User {
    //Attribute 
    protected String id;
    protected String username;
    protected String password;
    protected String fullName;

    public User(String id,String fullName,String username,String password){
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
    }
    public String getId(){
        return id;
    }
    public String getFullName(){
        return fullName;
    }
    public String getUsername(){
        return  username;
    }
    public String getPassword() {
        return password;
    }

    public void setFullName(String fullName){
        this.fullName = fullName;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public abstract String getRole();


}
