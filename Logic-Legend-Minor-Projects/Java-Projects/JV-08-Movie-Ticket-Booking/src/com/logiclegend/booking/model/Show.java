package com.logiclegend.booking.model;

public class Show {
    private int id;
    private int movieId;
    private String movieTitle;
    private String venue;
    private String screenName;
    private String showDate;
    private String showTime;
    private double silverPrice;
    private double goldPrice;
    private double platinumPrice;
    private String createdAt;

    public Show() {}

    public Show(int movieId, String venue, String screenName, String showDate, String showTime,
                double silverPrice, double goldPrice, double platinumPrice) {
        this.movieId = movieId;
        this.venue = venue;
        this.screenName = screenName;
        this.showDate = showDate;
        this.showTime = showTime;
        this.silverPrice = silverPrice;
        this.goldPrice = goldPrice;
        this.platinumPrice = platinumPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }

    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }

    public double getSilverPrice() { return silverPrice; }
    public void setSilverPrice(double silverPrice) { this.silverPrice = silverPrice; }

    public double getGoldPrice() { return goldPrice; }
    public void setGoldPrice(double goldPrice) { this.goldPrice = goldPrice; }

    public double getPlatinumPrice() { return platinumPrice; }
    public void setPlatinumPrice(double platinumPrice) { this.platinumPrice = platinumPrice; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%s at %s (%s, %s %s)", movieTitle, venue, screenName, showDate, showTime);
    }
}

