package muse_kopis.muse.photo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse_kopis.muse.ticketbook.domain.TicketBook;

@Entity
@Getter
@NoArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length=2048)
    private String url;
    @ManyToOne
    @JoinColumn(name = "ticket_book_id")
    private TicketBook ticketBook;

    public Photo(String url, TicketBook ticketBook) {
        this.url = url;
        this.ticketBook = ticketBook;
    }
}
