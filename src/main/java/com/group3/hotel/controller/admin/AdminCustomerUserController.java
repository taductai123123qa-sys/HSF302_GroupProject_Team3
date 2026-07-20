package com.group3.hotel.controller.admin;

import com.group3.hotel.dto.request.CustomerUserUpsertRequest;
import com.group3.hotel.exception.BadRequestException;
import com.group3.hotel.exception.ResourceNotFoundException;
import com.group3.hotel.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminCustomerUserController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("customers", adminCustomerService.getAllCustomers());
        return "admin/user-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("userForm", CustomerUserUpsertRequest.builder().build());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        var dto = adminCustomerService.getCustomerById(id);
        CustomerUserUpsertRequest form = CustomerUserUpsertRequest.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .build();
        model.addAttribute("userForm", form);
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("userForm") CustomerUserUpsertRequest form,
                           RedirectAttributes redirectAttributes) {
        try {
            adminCustomerService.saveCustomer(form);
            redirectAttributes.addFlashAttribute("success",
                    form.getId() == null ? "Thêm khách hàng thành công" : "Cập nhật khách hàng thành công");
        } catch (BadRequestException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            if (form.getId() != null) {
                return "redirect:/admin/users/edit/" + form.getId();
            }
            return "redirect:/admin/users/add";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminCustomerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khách hàng thành công");
        } catch (BadRequestException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa khách hàng: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
