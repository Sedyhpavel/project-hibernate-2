package com.javarush.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(schema = "movie", name = "store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Short id;

    @OneToOne
    @JoinColumn(name = "manager_staff_id", nullable = false)
    private Staff manager;

    @OneToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public Staff getManager() { return manager; }
    public void setManager(Staff manager) { this.manager = manager; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
