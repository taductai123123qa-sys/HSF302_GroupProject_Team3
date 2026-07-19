package com.group3.hotel.controller.reception;

import com.group3.hotel.entity.Receptionist;
import com.group3.hotel.repository.ReceptionistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/reception/settings")
@RequiredArgsConstructor
public class ReceptionSettingsController {

    private final ReceptionistRepository receptionistRepository;

    @GetMapping
    public String viewSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Receptionist> receptionistOpt = receptionistRepository.findByUserEmail(email);

        model.addAttribute("email", email);
        if (receptionistOpt.isPresent()) {
            model.addAttribute("receptionist", receptionistOpt.get());
        }

        return "reception/settings";
    }

    @PostMapping
    public String updateSettings(@RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String shiftType,
            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Receptionist> receptionistOpt = receptionistRepository.findByUserEmail(email);
        if (receptionistOpt.isPresent()) {
            Receptionist receptionist = receptionistOpt.get();
            receptionist.setFullName(fullName);
            receptionist.setPhone(phone);
            receptionist.setShiftType(shiftType);
            receptionistRepository.save(receptionist);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy hồ sơ Lễ tân để cập nhật.");
        }

        return "redirect:/reception/settings";
    }
}
