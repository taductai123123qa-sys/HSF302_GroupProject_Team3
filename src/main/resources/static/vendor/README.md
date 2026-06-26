# hãy static/vendor/ — Quản lý thư viện Frontend

Thư mục này chứa các thư viện frontend (CSS/JS) được dùng **offline** (không phụ thuộc CDN).

## Cách tổ chức

```
vendor/
  ├── bootstrap/          ← Bootstrap 5.x
  │   ├── css/
  │   │   └── bootstrap.min.css
  │   └── js/
  │       └── bootstrap.bundle.min.js
  │
  ├── fontawesome/        ← Font Awesome Icons
  │   ├── css/
  │   │   └── all.min.css
  │   └── webfonts/
  │       └── (font files)
  │
  ├── flatpickr/          ← Date/Time Picker
  │   ├── flatpickr.min.css
  │   └── flatpickr.min.js
  │
  └── datatables/         ← Table với sort/filter/pagination
      ├── datatables.min.css
      └── datatables.min.js
```

## Cách dùng trong Thymeleaf templates

```html
<!-- Bootstrap CSS -->
<link rel="stylesheet" th:href="@{/vendor/bootstrap/css/bootstrap.min.css}">

<!-- Font Awesome -->
<link rel="stylesheet" th:href="@{/vendor/fontawesome/css/all.min.css}">

<!-- Bootstrap JS (đặt trước </body>) -->
<script th:src="@{/vendor/bootstrap/js/bootstrap.bundle.min.js}"></script>
```

## Tại sao dùng vendor/ thay vì CDN?

|                              | CDN  | vendor/ |
| ---------------------------- | ---- | ------- |
| Hoạt động offline         | ❌   | ✅      |
| Tốc độ khi Internet chậm | ❌   | ✅      |
| Kiểm soát version          | ⚠️ | ✅      |
| Dung lượng repo            | ✅   | ⚠️    |

## Hướng dẫn tải về thư viện

### Bootstrap 5

```
https://getbootstrap.com/docs/5.3/getting-started/download/
```

### Font Awesome 6 (Free)

```
https://fontawesome.com/download
```

### Flatpickr

```
https://flatpickr.js.org/
```

### DataTables

```
https://datatables.net/download/
```
