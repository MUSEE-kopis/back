package muse_kopis.muse.auth.admin.application;

import lombok.RequiredArgsConstructor;
import muse_kopis.muse.auth.PasswordEncoder;
import muse_kopis.muse.auth.admin.domain.dto.AdminInfo;
import muse_kopis.muse.common.auth.UnAuthorizationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PasswordEncoder passwordEncoder;
    @Value("${admin.id}")
    private String adminId;
    @Value("${admin.password-hash}")
    private String passwordHash;
    @Value("${admin.LongId}")
    private Long adminLongId;

    public Long login(AdminInfo adminInfo) {
        if (!adminInfo.adminId().equals(adminId) ||
                !passwordEncoder.checkPassword(adminInfo.password(), passwordHash)) {
            throw new UnAuthorizationException("관리자 권한이 없습니다.");
        }
        return adminLongId;
    }
}
