package muse_kopis.muse.actor.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse_kopis.muse.ticketbook.domain.TicketBook;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TicketBookActor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "ticket_book_id")
    private TicketBook ticketBook;
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Actor actor;

    public void ticketBook(TicketBook ticketBook) {
        this.ticketBook = ticketBook;
    }
}
