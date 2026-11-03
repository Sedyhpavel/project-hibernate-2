package com.javarush.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(schema = "movie", name = "language")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "language_id")
    private Short id;

    @Column(name = "name", nullable = false, columnDefinition = "char(20)")
    private String name;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    /** Колонка char(20) – MySQL возвращает значение без хвостовых пробелов, но trim() не повредит. */
    public String getName() { return name == null ? null : name.trim(); }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
