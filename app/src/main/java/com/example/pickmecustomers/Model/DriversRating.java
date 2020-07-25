package com.example.pickmecustomers.Model;

public class DriversRating {

    String rating;
    String comment;
    String customer_id;

    public DriversRating() {
    }

    public DriversRating(String rating, String comment, String customer_id) {
        this.rating = rating;
        this.comment = comment;
        this.customer_id = customer_id;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }
}
