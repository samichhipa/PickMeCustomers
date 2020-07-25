package com.example.pickmecustomers.Model;

public class Customers {

    String id,name,password,customer_phone,customer_email;

    public Customers() {
    }

    public Customers(String id, String name, String password, String customer_phone, String customer_email) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.customer_phone = customer_phone;
        this.customer_email = customer_email;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCustomer_phone() {
        return customer_phone;
    }

    public void setCustomer_phone(String customer_phone) {
        this.customer_phone = customer_phone;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }
}
