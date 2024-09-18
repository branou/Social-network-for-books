package com.himbra.book.book;

import com.himbra.book.file.FileUtils;
import com.himbra.book.history.BookTransactionHistory;

public class BookMapper {
    public static BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .rate(book.getRate())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .owner(book.getOwner().getFullName())
                .cover(FileUtils.readFileFromLocation(book.getBookCover()))
                .build();
    }

    public static BookBorrowedResponse toBookBorrowedResponse(BookTransactionHistory bookTransactionHistory) {
        return BookBorrowedResponse.builder()
                .id(bookTransactionHistory.getBook().getId())
                .authorName(bookTransactionHistory.getBook().getAuthorName())
                .title(bookTransactionHistory.getBook().getTitle())
                .isbn(bookTransactionHistory.getBook().getIsbn())
                .rate(bookTransactionHistory.getBook().getRate())
                .returned(bookTransactionHistory.isReturned())
                .returnedApproved(bookTransactionHistory.isReturnApproved())
                .build();
    }
}
