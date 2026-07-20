package com.group3.hotel.controller;

import com.group3.hotel.entity.RoomCategory;
import com.group3.hotel.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
public class RoomCategoryController {

    private final RoomCategoryRepository categoryRepository;

    @Value("${app.upload.rooms-dir:uploads/rooms}")
    private String uploadDir;

    public RoomCategoryController(RoomCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/category-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new RoomCategory());
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        RoomCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ID: " + id));
        model.addAttribute("category", category);
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") RoomCategory category,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) {

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFileName = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(uniqueFileName);

                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                category.setImgUrl("/images/rooms/" + uniqueFileName);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi khi lưu ảnh: " + e.getMessage());
                return "redirect:/admin/categories/edit/" + category.getId();
            }
        } else if (category.getId() != null) {
            RoomCategory existingCategory = categoryRepository.findById(category.getId()).orElse(null);
            if (existingCategory != null) {
                category.setImgUrl(existingCategory.getImgUrl());
            }
        }

        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success",
                category.getId() == null ? "Thêm hạng phòng thành công" : "Cập nhật hạng phòng thành công");
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa hạng phòng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xóa hạng phòng này vì đang có phòng vật lý liên quan.");
        }
        return "redirect:/admin/categories";
    }
}
