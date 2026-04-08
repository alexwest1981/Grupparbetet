package Wigell.Sushi.API.entity;

import jakarta.persistence.*;

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int maxGuests;
    private String technicalEquipment; // Assuming this is a simple string for now

    public Room() {
    }

    public Room(String name, int maxGuests, String technicalEquipment) {
        this.name = name;
        this.maxGuests = maxGuests;
        this.technicalEquipment = technicalEquipment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getTechnicalEquipment() {
        return technicalEquipment;
    }

    public void setTechnicalEquipment(String technicalEquipment) {
        this.technicalEquipment = technicalEquipment;
    }
}
