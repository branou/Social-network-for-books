package com.himbra.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory,Long> {
    @Query("""
select history from BookTransactionHistory history where history.book.owner.id=:user_Id
""")
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable,long user_Id);
    @Query("""
select history from BookTransactionHistory history where history.user.id=:user_Id
""")
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable,long user_Id);
    @Query("""
 SELECT (COUNT (*) > 0) AS isBorrowed
        FROM BookTransactionHistory bookTransactionHistory
        WHERE bookTransactionHistory.user.id = :userId
        AND bookTransactionHistory.book.id = :bookId
        AND bookTransactionHistory.returnApproved = false
""")
    boolean isAlreadyBorrowedByUser(long bookId, Long userId);
    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM BookTransactionHistory bookTransactionHistory
            WHERE bookTransactionHistory.book.id = :bookId
            AND bookTransactionHistory.returnApproved = false
            """)
    boolean isAlreadyBorrowed(long bookId);
    @Query("""
            SELECT transaction
            FROM BookTransactionHistory  transaction
            WHERE transaction.user.id = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = false
            AND transaction.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(Long bookId, Long userId);
    @Query("""
            SELECT transaction
            FROM BookTransactionHistory  transaction
            WHERE transaction.book.owner.id = :userId
            AND transaction.book.id = :bookId
            AND transaction.returned = true
            AND transaction.returnApproved = false
            """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(long bookId, Long userId);
}
