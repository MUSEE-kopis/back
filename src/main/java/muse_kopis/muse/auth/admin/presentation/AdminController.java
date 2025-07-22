package muse_kopis.muse.auth.admin.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.admin.application.AdminService;
import muse_kopis.muse.auth.admin.domain.dto.AdminInfo;
import muse_kopis.muse.auth.jwt.JwtService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final JwtService jwtService;
    private final AdminService adminService;

    @GetMapping
    public String admin() {
        return "login";
    }

    @PostMapping("/login")
    public String adminPerformanceEditPage(@ModelAttribute AdminInfo adminInfo, Model model) {
        Long adminId = adminService.login(adminInfo);
        String token = jwtService.createToken(adminId);
        log.info("admin token = {}", token);
        model.addAttribute("token", token);
        return "search";
    }
}
