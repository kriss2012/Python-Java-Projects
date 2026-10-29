package com.logiclegend.booking.model;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private int durationMinutes;
    private String language;
    private String rating;
    private String synopsis;
    private String createdAt;

    public Movie() {}

    public Movie(String title, String genre, int durationMinutes, String language, String rating, String synopsis) {
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.rating = rating != null ? rating : "UA";
        this.synopsis = synopsis;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%s (%s, %d mins, %s)", title, language, durationMinutes, rating);
    }
}

