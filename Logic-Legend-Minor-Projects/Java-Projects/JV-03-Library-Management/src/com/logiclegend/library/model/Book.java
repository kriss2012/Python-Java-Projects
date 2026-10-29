package com.logiclegend.library.model;

public class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private String edition;
    private int totalCopies;
    private int availableCopies;
    private String shelfLocation;
    private String createdAt;

    public Book() {}

    public Book(String isbn, String title, String author, String category,
                String publisher, String edition, int totalCopies, String shelfLocation) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.edition = edition;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.shelfLocation = shelfLocation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() { return availableCopies > 0; }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%d/%d Avail) - %s",
                isbn, title, author, availableCopies, totalCopies, shelfLocation);
    }
}

