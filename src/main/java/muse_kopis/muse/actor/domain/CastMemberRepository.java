package muse_kopis.muse.actor.domain;

import java.util.List;
import muse_kopis.muse.actor.domain.dto.ActorDto;
import muse_kopis.muse.performance.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CastMemberRepository extends JpaRepository<CastMember, Long> {

    List<CastMember> findAllByPerformanceAndActor_NameContaining(Performance performance, String actorName);
}
