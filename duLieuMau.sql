select *
from [dbo].[booking_details]

select *
from room_bookings

select *
from [dbo].[payments]




INSERT INTO rooms (room_number, room_status, category_id) VALUES 
('101', 'AVAILABLE', 1),
('102', 'AVAILABLE', 1),
('103', 'OCCUPIED', 1),
('201', 'AVAILABLE', 2),
('202', 'NEED_CLEANING', 2),
('301', 'AVAILABLE', 3);