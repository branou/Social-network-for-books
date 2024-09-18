package com.himbra.book.feedback;

import com.himbra.book.base.PageResponse;
import com.himbra.book.book.Book;
import com.himbra.book.book.BookRepository;
import com.himbra.book.book.BookResponse;
import com.himbra.book.exception.OperationNotPermittedException;
import com.himbra.book.user.User;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service @RequiredArgsConstructor
public class FeedBackService {
    private final FeedbackRepository feedbackRepo;
    private final BookRepository bookRepo;
    public Long saveFeedback(FeedbackRequest request, Authentication auth) {
        Book book = bookRepo.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + request.bookId()));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("You cannot give a feedback for and archived or not shareable book");
        }
        User user = ((User) auth.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot give feedback to your own book");
        }
        Feedback feedback = FeedbackMapper.toFeedback(request);
        return feedbackRepo.save(feedback).getId();
    }
    public PageResponse<FeedbackResponse> findAllFeedbacks(long bookId, int page, int size, Authentication authentication) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = (User) authentication.getPrincipal();
        Pageable pageable= PageRequest.of(page,size);
        Page<Feedback> feedbacks = feedbackRepo.findAllByBookId(bookId,pageable);
        List<FeedbackResponse> feedbackResponses=feedbacks.stream().map(fee->FeedbackMapper.toFeedbackResponse(user.getId(),fee)).toList();
        return new PageResponse<>(feedbackResponses,feedbacks.getNumber(),
                feedbacks.getSize(),feedbacks.getTotalElements(),feedbacks.getTotalPages(),
                feedbacks.isFirst(),feedbacks.isLast());
    }
}
