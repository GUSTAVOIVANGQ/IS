package com.web.book.version.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Show {
    private Long id;
    private String name;
    private String summary;
    private Image image;
    private String[] genres;
    private Rating rating;
    private String status;
    private String premiered;
    private String language;
    private String type;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String medium;
        private String original;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rating {
        private Double average;
    }

    // Helper methods
    public String getImageUrl() {
        return (image != null && image.getMedium() != null) ? image.getMedium() : "default-show-image.jpg";
    }

    public String getTitle() {
        return name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
}