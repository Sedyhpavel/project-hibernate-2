package com.javarush.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(schema = "movie", name = "film")
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "film_id")
    private Integer id;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "release_year", columnDefinition = "year")
    private Integer releaseYear;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @ManyToOne
    @JoinColumn(name = "original_language_id")
    private Language originalLanguage;

    @Column(name = "rental_duration", nullable = false)
    private Short rentalDuration;

    @Column(name = "rental_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal rentalRate;

    @Column(name = "length")
    private Integer length;

    @Column(name = "replacement_cost", nullable = false, precision = 5, scale = 2)
    private BigDecimal replacementCost;

    @Column(name = "rating", columnDefinition = "enum('G','PG','PG-13','R','NC-17')")
    @Convert(converter = RatingConverter.class)
    private Rating rating;

    /** MySQL SET хранится строкой вида "Trailers,Deleted Scenes"; наружу отдаём Set&lt;Feature&gt;. */
    @Column(name = "special_features",
            columnDefinition = "set('Trailers','Commentaries','Deleted Scenes','Behind the Scenes')")
    private String specialFeatures;

    @ManyToMany
    @JoinTable(schema = "movie", name = "film_actor",
            joinColumns = @JoinColumn(name = "film_id", referencedColumnName = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "actor_id"))
    private Set<Actor> actors;

    @ManyToMany
    @JoinTable(schema = "movie", name = "film_category",
            joinColumns = @JoinColumn(name = "film_id", referencedColumnName = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "category_id"))
    private Set<Category> categories;

    @OneToOne(mappedBy = "film", fetch = FetchType.LAZY)
    private FilmText filmText;

    @Column(name = "last_update")
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    public Set<Feature> getSpecialFeatures() {
        if (specialFeatures == null || specialFeatures.isBlank()) {
            return EnumSet.noneOf(Feature.class);
        }
        return Arrays.stream(specialFeatures.split(","))
                .map(Feature::fromValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Feature.class)));
    }

    public void setSpecialFeatures(Set<Feature> features) {
        if (features == null || features.isEmpty()) {
            specialFeatures = null;
        } else {
            specialFeatures = features.stream().map(Feature::getValue).collect(Collectors.joining(","));
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public Language getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(Language originalLanguage) { this.originalLanguage = originalLanguage; }
    public Short getRentalDuration() { return rentalDuration; }
    public void setRentalDuration(Short rentalDuration) { this.rentalDuration = rentalDuration; }
    public BigDecimal getRentalRate() { return rentalRate; }
    public void setRentalRate(BigDecimal rentalRate) { this.rentalRate = rentalRate; }
    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }
    public BigDecimal getReplacementCost() { return replacementCost; }
    public void setReplacementCost(BigDecimal replacementCost) { this.replacementCost = replacementCost; }
    public Rating getRating() { return rating; }
    public void setRating(Rating rating) { this.rating = rating; }
    public Set<Actor> getActors() { return actors; }
    public void setActors(Set<Actor> actors) { this.actors = actors; }
    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }
    public FilmText getFilmText() { return filmText; }
    public void setFilmText(FilmText filmText) { this.filmText = filmText; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
