package com.himbra.book.feedback;

import com.himbra.book.base.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/feedbacks")
@Tag(name="Feedback")
public class FeedbackController {
    private final FeedBackService feedBackService;
    @PostMapping("/saveFeedback")
    public ResponseEntity<Long> saveFeedback(@RequestBody @Valid FeedbackRequest feedback, Authentication auth){
        return ResponseEntity.ok(feedBackService.saveFeedback(feedback,auth));
    }
    @GetMapping("/book/{bookId}")
    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbacks(@PathVariable("bookId") long bookId,
     @RequestParam(name="page",defaultValue = "0",required = false) int page,
    @RequestParam(name = "size", defaultValue = "10",required = false) int size,
    Authentication authentication)
    {
        return ResponseEntity.ok(feedBackService.findAllFeedbacks(bookId,page,size,authentication));
    }
}
