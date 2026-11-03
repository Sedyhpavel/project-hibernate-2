package com.javarush.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(schema = "movie", name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Short id;

    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    @OneToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "picture", columnDefinition = "blob")
    private byte[] picture;

    @Column(name = "email", length = 50)
    private String email;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "active", nullable = false, columnDefinition = "tinyint(1)")
    private Boolean active;

    @Column(name = "username", nullable = false, length = 16)
    private String username;

    @Column(name = "password", length = 40)
    private String password;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public byte[] getPicture() { return picture; }
    public void setPicture(byte[] picture) { this.picture = picture; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
