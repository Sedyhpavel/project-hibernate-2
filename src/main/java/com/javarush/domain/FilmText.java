package com.javarush.domain;

import jakarta.persistence.*;

/**
 * film_text: film_id – одновременно PK и (логически) ссылка на film.film_id.
 * В БД foreign key нет, но в модели связь OneToOne с общим ключом (@MapsId).
 */
@Entity
@Table(schema = "movie", name = "film_text")
public class FilmText {
    @Id
    @Column(name = "film_id")
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "film_id")
    private Film film;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Film getFilm() { return film; }
    public void setFilm(Film film) { this.film = film; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
