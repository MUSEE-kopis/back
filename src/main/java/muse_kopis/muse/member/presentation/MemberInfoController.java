package muse_kopis.muse.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import muse_kopis.muse.auth.Auth;
import muse_kopis.muse.member.application.MemberInfoService;
import muse_kopis.muse.member.domain.dto.MemberInfoRequest;
import muse_kopis.muse.member.domain.dto.MemberInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memberInfo")
@RequiredArgsConstructor
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    @Operation(summary = "사용자 정보", description = "모델 학습을 위해 사용자 정보를 수집한 것을 조회합니다.")
    @GetMapping
    public ResponseEntity<MemberInfoResponse> getMemberInfo(@Auth Long memberId) {
        return ResponseEntity.ok().body(memberInfoService.getMemberInfo(memberId));
    }

    @Operation(summary = "사용자 정보 저장", description = "모델 학습을 위한 사용자 정보를 수집, 저장 합니다.")
    @PostMapping
    public ResponseEntity<Void> createMemberInfo(@Auth Long memberId, @RequestBody MemberInfoRequest request) {
        memberInfoService.createMemberInfo(memberId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 정보 삭제", description = "수집한 사용자 정보를 삭제 합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteMemberInfo(@Auth Long memberId) {
        memberInfoService.deleteMemberInfo(memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 정보 갱신", description = "수집한 사용자 정보를 수정 합니다.")
    @PatchMapping
    public ResponseEntity<Void> updateMemberInfo(@Auth Long memberId, @RequestBody MemberInfoRequest request) {
        memberInfoService.updateMemberInfo(memberId, request);
        return ResponseEntity.ok().build();
    }
}
