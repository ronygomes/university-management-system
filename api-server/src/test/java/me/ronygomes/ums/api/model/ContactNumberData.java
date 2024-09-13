package me.ronygomes.ums.api.model;

import me.ronygomes.ums.api.validator.annotation.ContactNumber;

public class ContactNumberData {

    @ContactNumber
    private String contactNumber;

    @ContactNumber(message = "Custom Message")
    private String contactNumber2;

    public ContactNumberData(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactNumber2() {
        return contactNumber2;
    }

    public void setContactNumber2(String contactNumber2) {
        this.contactNumber2 = contactNumber2;
    }
}
