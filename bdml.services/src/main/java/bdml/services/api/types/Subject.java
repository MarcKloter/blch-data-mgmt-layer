package bdml.services.api.types;

public class Subject {
    // TODO: validate structure

    private String address;
    private String description;

    public Subject(String address, String description) {
        this.address = address;
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
