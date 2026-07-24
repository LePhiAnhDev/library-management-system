-- Standard set of book categories so the catalog is usable out of the box.
INSERT INTO categories (name, description, status, created_at, updated_at) VALUES
    ('Văn học', 'Tiểu thuyết, truyện ngắn, thơ ca', 'ACTIVE', now(), now()),
    ('Khoa học', 'Khoa học tự nhiên và xã hội', 'ACTIVE', now(), now()),
    ('Công nghệ thông tin', 'Lập trình, mạng, hệ thống', 'ACTIVE', now(), now()),
    ('Kinh tế', 'Kinh tế, tài chính, quản trị', 'ACTIVE', now(), now()),
    ('Lịch sử', 'Lịch sử Việt Nam và thế giới', 'ACTIVE', now(), now()),
    ('Thiếu nhi', 'Sách dành cho trẻ em', 'ACTIVE', now(), now()),
    ('Ngoại ngữ', 'Học và luyện ngoại ngữ', 'ACTIVE', now(), now()),
    ('Tâm lý - Kỹ năng', 'Tâm lý học và kỹ năng sống', 'ACTIVE', now(), now()),
    ('Nghệ thuật', 'Hội họa, âm nhạc, nhiếp ảnh', 'ACTIVE', now(), now()),
    ('Tham khảo', 'Từ điển, bách khoa, giáo trình', 'ACTIVE', now(), now());
