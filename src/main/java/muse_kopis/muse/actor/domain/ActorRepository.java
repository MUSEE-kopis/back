package muse_kopis.muse.actor.domain;

import java.util.List;
import java.util.Optional;
import muse_kopis.muse.actor.domain.dto.ActorDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {
    List<ActorDto> findAllByNameIsContaining(String actorName);
    Actor findByActorId(String actorId);
    Optional<Actor> findByName(String name);
}
