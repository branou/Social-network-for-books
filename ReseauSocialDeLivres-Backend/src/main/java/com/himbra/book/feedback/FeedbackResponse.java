package com.himbra.book.feedback;

import lombok.*;

@Builder @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class FeedbackResponse {
    private Double note;
    private String comment;
    private boolean ownFeedback;
}
