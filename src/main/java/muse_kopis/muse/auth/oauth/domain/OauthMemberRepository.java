package muse_kopis.muse.auth.oauth.domain;

import java.util.Optional;
import muse_kopis.muse.common.member.NotFoundMemberException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthMemberRepository extends JpaRepository<OauthMember, Long> {

    default OauthMember getByOauthMemberId(Long memberId) {
        return findById(memberId).orElseThrow(() -> new NotFoundMemberException("사용자를 찾을 수 없습니다."));
    }

    Optional<OauthMember> findByOauthId(OauthId oauthId);
}