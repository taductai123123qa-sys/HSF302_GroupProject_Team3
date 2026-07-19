package com.group3.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình phục vụ các file tĩnh được upload lên runtime (ví dụ: ảnh hạng phòng).
 * Khi upload qua controller, file được lưu ra thư mục bên ngoài project
 * (tránh việc phải rebuild jar mỗi lần upload). Mapping này cho phép trình duyệt
 * truy cập ảnh thông qua URL "/images/rooms/ten_file.jpg".
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.rooms-dir:uploads/rooms}")
    private String roomsUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(roomsUploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();

        registry.addResourceHandler("/images/rooms/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
