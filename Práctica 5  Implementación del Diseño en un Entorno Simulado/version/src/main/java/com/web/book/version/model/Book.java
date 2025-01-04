package com.web.book.version.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String title;
    private String author;
    private String publishYear;
    private String isbn;
    private String rating;
    private String coverUrl;
    private String description;
    private String publisher;
    private String[] genres;

    // Constructor básico que inicializa campos obligatorios
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.rating = "0.0";
        this.coverUrl = "default-book-cover.jpg";
    }
}