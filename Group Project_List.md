**Group Project List**

Course: HSF302 \- Spring Boot Web MVC | Weight: 25% of total grade

**General Information**

**Team size:** 4-5 members per group

**Workload:** Each member is responsible for 3-5 screens of simple to medium complexity **Business requirement:** At least 2-3 main business flows

**Goal:** Practice core skills of Spring Boot Web MVC

Spring Boot Spring MVC Spring Data JPA Spring Security Thymeleaf MySQL / SQL Server Bootstrap / Tailwind

**Common Technical Requirements (applied to all topics)**

**Authentication & Authorization:** Register, login, role-based access (minimum 2 roles) using Spring Security

**Full CRUD:** Create / Read / Update / Delete for main entities

**Paging & Sorting:** For long lists

**Search & Filter:** By multiple criteria

**File upload:** Images / documents (avatar, product image, CV...)

**Validation:** Server-side form validation (Bean Validation)

**Exception Handling:** Custom 404, 403, 500 pages

**Thymeleaf:** Layout fragments, form binding, conditional rendering

**3 Hotel Booking System**

A website that allows guests to search for available hotel rooms by date range and book them online, while receptionists handle check-in/check-out.

Website cho phép khách hàng tìm kiếm phòng khách sạn theo khoảng thời gian và đặt phòng online; lễ tân xử lý check-in / check-out.

**ACTORS**

Guest Receptionist Admin

**MAIN BUSINESS FLOWS**

Guest: search rooms by date → view detail → book room → pay (simulated) Receptionist: confirm booking → check-in → check-out

Admin: manage rooms, room types, services, seasonal pricing

**USE CASE TABLE**

**ID Actor Use Case Description UC01** All Register / Login Create account / log in **UC02** Guest Search Available Rooms Filter by date, room type, capacity **UC03** Guest View Room Detail See photos, amenities, price

**UC04** Guest Book Room Submit booking with date range and guest count

**UC05** Guest Cancel Booking Cancel a pending booking **UC06** Guest View Booking History List all past and upcoming stays **UC07** Receptionist Confirm Booking Approve / reject booking requests **UC08** Receptionist Check-in / Check-out Update guest status

**UC09** Guest Request Room Change Submit a room change request during stay
**ID Actor Use Case Description UC10** Admin Manage Rooms CRUD with image upload

**UC11** Admin Manage Services CRUD spa, breakfast, and shuttle services

**UC12** Admin Revenue Statistics Report on occupancy and revenue

**REPRESENTATIVE SCREENS (15-18)**

Home \+ Room Search Room List Detail \+ Book Booking History Room Management Booking Management Revenue Report

**4Suggested Grading Criteria**

**Installation & Demo (20%):** Code runs and is deployable, with seed data **Business Logic (25%):** Includes 2-3 clear main business flows

**Spring Techniques (30%):** Correct use of Spring Security, JPA, Validation, Exception handling

**UI/UX with Thymeleaf (15%):** Reasonable layout, fragments, basic responsiveness

**Report & Presentation (10%):** Clear documentation and transparent task distribution
