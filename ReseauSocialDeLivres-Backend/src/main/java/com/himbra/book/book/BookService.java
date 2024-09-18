package com.himbra.book.book;

import com.himbra.book.base.PageResponse;
import com.himbra.book.exception.OperationNotPermittedException;
import com.himbra.book.file.FileStorageService;
import com.himbra.book.history.BookTransactionHistory;
import com.himbra.book.history.BookTransactionHistoryRepository;
import com.himbra.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service @RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final FileStorageService fileStorageService;
    public Long save(BookRequest req, Authentication auth) {
        User user =(User) auth.getPrincipal();
        Book book = Book.builder()
                .id(req.id()).title(req.title()).authorName(req.authorName())
                .isbn(req.isbn()).synopsis(req.synopsis())
                .archived(false).shareable(req.shareable()).build();
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }
    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
                .map(BookMapper::toBookResponse)
                .orElseThrow(()-> new EntityNotFoundException("book with this id doesn't exist"));
    }
    public PageResponse<BookResponse> findAllBooks(int page,int size,Authentication auth){
        User user=(User) auth.getPrincipal();
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable,user.getId());
        List<BookResponse> bookResponses = books.stream().map(BookMapper::toBookResponse).toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication authentication) {
        Pageable pageable = PageRequest.of(page,size,Sort.by("createdDate").ascending());
        User userId=(User) authentication.getPrincipal();
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(userId.getId()), pageable);
        List<BookResponse> bookResponses = books.stream().map(BookMapper::toBookResponse).toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }
    public PageResponse<BookBorrowedResponse> findAllBorrowedBooks(int page, int size, Authentication authentication) {
        User user=(User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page,size,Sort.by("createdDate").descending());
        Page<BookTransactionHistory> booksBorrowed = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable,user.getId());
        List<BookBorrowedResponse> bookResponses = booksBorrowed.stream().map(BookMapper::toBookBorrowedResponse).toList();
        return new PageResponse<>(
                bookResponses,
                booksBorrowed.getNumber(),
                booksBorrowed.getSize(),
                booksBorrowed.getTotalElements(),
                booksBorrowed.getTotalPages(),
                booksBorrowed.isFirst(),
                booksBorrowed.isLast()
        );
    }
    public PageResponse<BookBorrowedResponse> findAllReturnedBooks(int page, int size, Authentication authentication) {
        User user=(User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page,size,Sort.by("createdDate").descending());
        Page<BookTransactionHistory> booksBorrowed = bookTransactionHistoryRepository.findAllReturnedBooks(pageable,user.getId());
        List<BookBorrowedResponse> bookResponses = booksBorrowed.stream().map(BookMapper::toBookBorrowedResponse).toList();
        return new PageResponse<>(
                bookResponses,
                booksBorrowed.getNumber(),
                booksBorrowed.getSize(),
                booksBorrowed.getTotalElements(),
                booksBorrowed.getTotalPages(),
                booksBorrowed.isFirst(),
                booksBorrowed.isLast()
        );
    }

    public Long updateShareableStatus(long bookId, Authentication auth) {
        Book book = bookRepository.findById(bookId).orElseThrow(()-> new EntityNotFoundException("book not found with this id "+bookId));
        User user=(User) auth.getPrincipal();
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("you don't have the right to update the sharable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;

    }
    public Long updateArchivedStatus(long bookId, Authentication auth) {
        Book book = bookRepository.findById(bookId).orElseThrow(()-> new EntityNotFoundException("book not found with this id "+bookId));
        User user=(User) auth.getPrincipal();
        if(!Objects.equals(book.getOwner().getId(), user.getId())){
            throw new OperationNotPermittedException("you don't have the right to update the archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Long borrowBook(long bookId, Authentication auth) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or is not shareable");
        }
        User user = ((User) auth.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowedByUser = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowedByUser) {
            throw new OperationNotPermittedException("You already borrowed this book and it is still not returned or the return is not approved by the owner");
        }

        final boolean isAlreadyBorrowedByOtherUser = bookTransactionHistoryRepository.isAlreadyBorrowed(bookId);
        if (isAlreadyBorrowedByOtherUser) {
            throw new OperationNotPermittedException("Te requested book is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Long returnBorrowBook(long bookId, Authentication auth) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be returned since it is archived or is not shareable");
        }
        User user = ((User) auth.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot return or borrow your own book");
        }
        BookTransactionHistory bookTH=bookTransactionHistoryRepository.findByBookIdAndUserId(bookId,user.getId()).orElseThrow(()-> new OperationNotPermittedException("this book thi"));
        bookTH.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTH).getId();
    }

    public Long ApproveReturnBorrowedBook(long bookId, Authentication auth) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be approved since it is archived or is not shareable");
        }
        User user = ((User) auth.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow a book you don't own");
        }
        BookTransactionHistory bookTH=bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId,user.getId()).orElseThrow(()-> new OperationNotPermittedException("this book thi"));
        bookTH.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTH).getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        var profilePicture = fileStorageService.saveFile(file, bookId, user.getId());
        book.setBookCover(profilePicture);
        bookRepository.save(book);
    }
}
