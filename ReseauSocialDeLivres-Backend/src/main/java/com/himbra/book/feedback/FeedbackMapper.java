package com.himbra.book.feedback;

import com.himbra.book.book.Book;

import java.util.Objects;

public class FeedbackMapper {
    public static Feedback toFeedback(FeedbackRequest feedback){
        return Feedback.builder()
                .note(feedback.note())
                .comment(feedback.comment())
                .book(Book.builder().id(feedback.bookId()).build())
                .build();
    }

    public static FeedbackResponse toFeedbackResponse(long id,Feedback feedback) {
        return FeedbackResponse.builder()
                .ownFeedback(Objects.equals(id,feedback.getCreatedBy()))
                .note(feedback.getNote())
                .comment(feedback.getComment())
                .build();
    }
}
