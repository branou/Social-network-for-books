package com.himbra.book.book;

import com.himbra.book.base.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("books")
@Tag(name="Book")
public class BookController {
    private final BookService bookService;
    @PostMapping
    public ResponseEntity<Long> saveBook(@Valid @RequestBody BookRequest req, Authentication auth){
        return ResponseEntity.ok(bookService.save(req,auth));
    }
    @GetMapping("{bookId}")
    public ResponseEntity<BookResponse> findBookById(@PathVariable("bookId") Long bookId){
        return ResponseEntity.ok(bookService.findById(bookId));
    }
    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(
            @RequestParam(name="page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size", defaultValue = "10",required = false) int size,
            Authentication authentication
    ){
        return ResponseEntity.ok(bookService.findAllBooks(page,size,authentication));
    }
    @GetMapping("/owner")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name="page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size", defaultValue = "10",required = false) int size,
            Authentication authentication){
        return ResponseEntity.ok(bookService.findAllBooksByOwner(page,size,authentication));
    }
    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BookBorrowedResponse>> findAllBorrowedBooks(
            @RequestParam(name="page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size", defaultValue = "10",required = false) int size,
            Authentication authentication){
        return ResponseEntity.ok(bookService.findAllBorrowedBooks(page,size,authentication));
    }
    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BookBorrowedResponse>> findAllReturnedBooks(
            @RequestParam(name="page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size", defaultValue = "10",required = false) int size,
            Authentication authentication){
        return ResponseEntity.ok(bookService.findAllReturnedBooks(page,size,authentication));
    }
    @PatchMapping("/shareable/{bookId}")
    public ResponseEntity<Long> updateSharableStatus(@PathVariable("bookId") long bookId, Authentication auth){
        return ResponseEntity.ok(bookService.updateShareableStatus(bookId,auth));
    }

    @PatchMapping("/archived/{bookId}")
    public ResponseEntity<Long> updateArchivedStatus(@PathVariable("bookId") long bookId, Authentication auth){
        return ResponseEntity.ok(bookService.updateArchivedStatus(bookId,auth));
    }

    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<Long> borrowBook(@PathVariable("bookId") long bookId, Authentication auth){
        return ResponseEntity.ok(bookService.borrowBook(bookId,auth));
    }
    @PatchMapping("/borrow/return/{bookId}")
    public ResponseEntity<Long> returnBorrowBook(@PathVariable("bookId") long bookId, Authentication auth){
        return ResponseEntity.ok(bookService.returnBorrowBook(bookId,auth));
    }
    @PatchMapping("/borrow/return/approve{bookId}")
    public ResponseEntity<Long> approveReturnBorrowedBook(@PathVariable("bookId") long bookId, Authentication auth){
        return ResponseEntity.ok(bookService.ApproveReturnBorrowedBook(bookId,auth));
    }
    @PostMapping(value = "/cover/{book-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadBookCoverPicture(
            @PathVariable("book-id") long bookId,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication connectedUser
    ) {
        bookService.uploadBookCoverPicture(file, connectedUser, bookId);
        return ResponseEntity.accepted().build();
    }
}
