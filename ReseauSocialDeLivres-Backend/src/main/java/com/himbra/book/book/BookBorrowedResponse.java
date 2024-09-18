package com.himbra.book.book;

import lombok.*;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class BookBorrowedResponse {
        private long id;
        private String title;
        private String authorName;
        private String isbn;
        private double rate;
        private boolean returned;
        private boolean returnedApproved;
}
