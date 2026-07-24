# Hướng dẫn sử dụng & Kịch bản demo hệ thống Quản lý Thư viện

Tài liệu này hướng dẫn thao tác hệ thống từ đầu tới cuối theo góc nhìn của một người
dùng mới vừa đăng nhập thành công, sắp xếp đúng theo trình tự để chạy demo khi thuyết trình.
Mỗi bước ghi rõ: vào trang nào (đường dẫn), bấm nút gì, điền nội dung ra sao, kết quả thấy được.

> Quy ước đọc: **Chữ đậm** là tên nút / nhãn / menu đúng như trên giao diện. `mã` là đường
> dẫn (route) hoặc giá trị cần gõ. Dấu → nghĩa là "rồi bấm/đi tới".

## Mục lục

1. [Chuẩn bị trước buổi demo (đọc kỹ phần này)](#1-chuẩn-bị-trước-buổi-demo-đọc-kỹ-phần-này)
2. [Đăng nhập](#2-đăng-nhập)
3. [Làm quen giao diện trong 30 giây](#3-làm-quen-giao-diện-trong-30-giây)
4. [Kịch bản demo end-to-end (phần chính)](#4-kịch-bản-demo-end-to-end-phần-chính)
5. [Đường demo lõi cho 7 phút (bản rút gọn)](#5-đường-demo-lõi-cho-7-phút-bản-rút-gọn)
6. [Bảng tra cứu nhanh các màn hình](#6-bảng-tra-cứu-nhanh-các-màn-hình)
7. [Lỗi thường gặp khi demo và cách xử lý](#7-lỗi-thường-gặp-khi-demo-và-cách-xử-lý)

---

## 1. Chuẩn bị trước buổi demo (đọc kỹ phần này)

**Địa chỉ truy cập:**

- Bản chạy thật (production): `https://drx.io.vn` (giao diện), API tại `https://api.drx.io.vn`.
- Bản chạy máy local (khi phát triển): `http://localhost:3000`.

**Cần có sẵn:**

- Một tài khoản đăng nhập (Clerk) đã đăng ký và đăng nhập được. Nếu chưa có, vào `/sign-up`
  đăng ký trước, đừng để tới lúc demo mới tạo.
- Một file ảnh (JPG/PNG) trên máy để minh họa chức năng **Tải ảnh bìa**.

> ### CẢNH BÁO QUAN TRỌNG: bẫy khi chạy lại nhiều lần
>
> Khi tập demo nhiều lần rồi chạy thật, các bước "thêm mới" dễ báo lỗi trùng vì những
> trường sau là **duy nhất trong hệ thống**:
>
> - **ISBN** của sách
> - **Mã vạch** bản sao (barcode)
> - **Email** độc giả
>
> Ngoài ra, một bản sao đã **Cho mượn** sẽ ở trạng thái **Đang mượn**, phải **Trả sách** xong
> mới cho mượn lại được.
>
> **Hai cách tránh (chọn 1):**
>
> 1. **Đổi giá trị mỗi lần chạy**: mỗi lần rehearse dùng ISBN / mã vạch / email mới, ví dụ
>    tăng số dần: mã vạch `DEMO-01`, `DEMO-02`, `DEMO-03`... email `demo01@thuvien.vn`,
>    `demo02@thuvien.vn`...
> 2. **An toàn nhất cho buổi live**: chuẩn bị sẵn dữ liệu (sách + bản sao + độc giả) từ hôm
>    trước, khi thuyết trình chỉ chạy phần gây ấn tượng: **Cho mượn → Gia hạn → Trả sách →
>    Phạt → Báo cáo** trên dữ liệu đã có. Ít rủi ro nhất.
>
> Mã thẻ độc giả (**member code**) do hệ thống tự sinh nên không lo trùng.

**Dữ liệu có sẵn khi cài đặt:** hệ thống đã tạo sẵn **10 thể loại** (Văn học, Khoa học,
Công nghệ thông tin, Kinh tế, Lịch sử, Thiếu nhi, Ngoại ngữ, Tâm lý - Kỹ năng, Nghệ thuật,
Tham khảo). Vì vậy bạn có thể bỏ qua bước tạo thể loại và dùng luôn thể loại có sẵn. Tác giả,
nhà xuất bản, sách, độc giả thì bắt đầu từ trống, cần tự thêm.

**Bộ giá trị mẫu dùng xuyên suốt hướng dẫn** (bạn có thể đổi tùy ý):

| Mục | Giá trị mẫu |
| --- | --- |
| Tác giả | `Nguyễn Nhật Ánh` |
| Nhà xuất bản | `NXB Trẻ` |
| Sách | ISBN `DEMO-978-01`, tiêu đề `Cho tôi xin một vé đi tuổi thơ`, thể loại `Văn học`, năm `2008`, ngôn ngữ `Tiếng Việt`, số trang `208` |
| Bản sao | mã vạch `DEMO-01`, vị trí kệ `A1-01` |
| Độc giả | họ tên `Trần Văn An`, email `demo01@thuvien.vn`, loại thẻ `Sinh viên` |

---

## 2. Đăng nhập

1. Mở trình duyệt, vào `https://drx.io.vn`.
2. Vì chưa đăng nhập, hệ thống tự chuyển sang trang đăng nhập `/sign-in` (hiển thị ô đăng
   nhập của Clerk kèm logo và tên "Thư viện").
3. Nhập tài khoản đã có rồi đăng nhập (email + mật khẩu, hoặc phương thức đã cấu hình).
4. Đăng nhập thành công sẽ vào thẳng **Bảng điều khiển** ở trang chủ `/`.

> Nếu muốn xem trang đăng ký: `/sign-up`. Sau khi có tài khoản, mọi trang bên trong đều yêu
> cầu đã đăng nhập; truy cập trực tiếp khi chưa đăng nhập sẽ bị đưa về `/sign-in`.

---

## 3. Làm quen giao diện trong 30 giây

Sau khi đăng nhập, màn hình chia 3 khu vực:

**Thanh điều hướng bên trái** gồm 3 nhóm:

- **Thư viện**: **Bảng điều khiển** (`/`), **Danh mục sách** (`/books`), **Độc giả** (`/members`).
- **Lưu thông**: **Mượn / Trả** (`/circulation`), **Phiếu mượn** (`/loans`), **Đặt trước**
  (`/reservations`), **Phạt** (`/fines`).
- **Quản trị**: **Thể loại** (`/categories`), **Tác giả** (`/authors`), **Nhà xuất bản**
  (`/publishers`), **Báo cáo** (`/reports`), **Cấu hình** (`/settings`).

**Thanh trên cùng**: nút thu gọn/mở sidebar, breadcrumb chỉ vị trí hiện tại, ô **Tìm kiếm...**
(phím tắt `Ctrl + K`), và nút tài khoản ở góc phải (bấm để **Đăng xuất**).

**Bảng điều khiển (trang chủ)** hiển thị:

- 6 thẻ số liệu: **Đầu sách**, **Bản sao**, **Độc giả**, **Đang mượn**, **Quá hạn**,
  **Phạt đã thu (tháng)**.
- 2 danh sách: **Sách quá hạn** và **Sách được mượn nhiều**.

Với hệ thống mới, các số liệu đa phần bằng 0. Chúng ta sẽ tạo dữ liệu ở các bước sau và quay
lại đây để thấy số liệu thay đổi.

---

## 4. Kịch bản demo end-to-end (phần chính)

Trình tự dưới đây tuân theo đúng luồng nghiệp vụ thư viện. **Thứ tự có ràng buộc**: phải có
sách, sách phải có **bản sao (mã vạch)**, rồi mới cho mượn được. Các bước gắn nhãn **[LÕI]**
là đường demo tối thiểu gây ấn tượng; các bước **[TÙY CHỌN]** để trình bày thêm nếu còn thời gian.

### Bước 1 [TÙY CHỌN]: Thêm tác giả

1. Sidebar → **Quản trị → Tác giả** (`/authors`).
2. Bấm **Thêm tác giả** (góc phải trên).
3. Trong hộp thoại, điền:
   - **Tên tác giả** (bắt buộc): `Nguyễn Nhật Ánh`.
   - **Tiểu sử** (không bắt buộc): mô tả ngắn.
4. Bấm **Tạo**. Có thông báo "Đã tạo tác giả", tác giả xuất hiện trong bảng.

### Bước 2 [TÙY CHỌN]: Thêm nhà xuất bản

1. Sidebar → **Quản trị → Nhà xuất bản** (`/publishers`).
2. Bấm **Thêm nhà xuất bản**.
3. Điền **Tên nhà xuất bản** (bắt buộc): `NXB Trẻ`. Các ô **Địa chỉ**, **Điện thoại**,
   **Email** không bắt buộc.
4. Bấm **Tạo**.

> Thể loại đã có sẵn 10 mục nên không cần tạo. Nếu vẫn muốn: **Quản trị → Thể loại**
> (`/categories`) → **Thêm thể loại**, điền **Tên thể loại**, có thể chọn **Thể loại cha**
> để phân cấp.

### Bước 3 [LÕI]: Thêm sách

1. Sidebar → **Thư viện → Danh mục sách** (`/books`).
2. Bấm **Thêm sách**.
3. Điền vào hộp thoại **Thêm sách**:
   - **ISBN** (bắt buộc, **duy nhất**): `DEMO-978-01`.
   - **Tiêu đề** (bắt buộc): `Cho tôi xin một vé đi tuổi thơ`.
   - **Tiêu đề phụ**, **Mô tả**: không bắt buộc.
   - **Thể loại** (bắt buộc): bấm vào ô, gõ để tìm rồi chọn `Văn học`.
   - **Nhà xuất bản**: gõ tìm rồi chọn `NXB Trẻ` (không bắt buộc).
   - **Tác giả**: gõ tìm rồi chọn `Nguyễn Nhật Ánh`. Có thể chọn nhiều tác giả, mỗi tác giả
     hiện thành một thẻ nhỏ, bấm dấu **x** để bỏ.
   - **Năm xuất bản** `2008`, **Ngôn ngữ** `Tiếng Việt`, **Số trang** `208`: không bắt buộc.
4. Bấm **Tạo**. Có thông báo "Đã tạo sách". Sách hiện trong lưới với nhãn **Hết** vì chưa có
   bản sao nào.

> Màn hình danh mục sách còn có: ô tìm theo tiêu đề/ISBN, lọc theo thể loại, lọc trạng thái,
> ô tick **Chỉ còn sách**, và nút chuyển xem **dạng lưới / dạng danh sách** ở góc phải.

### Bước 4 [LÕI]: Mở chi tiết sách, tải ảnh bìa và thêm bản sao

1. Bấm vào tên (hoặc thẻ) cuốn sách vừa tạo để mở trang chi tiết `/books/{id}`.
2. **Tải ảnh bìa**: ở cột trái, bấm **Tải ảnh bìa** → chọn file ảnh trên máy. Sau vài giây có
   thông báo "Đã tải ảnh bìa" và ảnh hiện lên; nút đổi thành **Đổi ảnh bìa**.
3. **Thêm bản sao**: ở thẻ **Bản sao**, bấm **Thêm bản sao**, điền:
   - **Mã vạch** (bắt buộc, **duy nhất**): `DEMO-01`.
   - **Vị trí kệ**: `A1-01` (không bắt buộc).
   - **Ngày nhập**, **Ghi chú tình trạng**: không bắt buộc.
4. Bấm **Thêm**. Có thông báo "Đã thêm bản sao"; dòng **Bản sao sẵn sàng** tăng lên `1/1` và
   nhãn của sách đổi thành **Còn 1/1**.

> ### Đây là bước bắt buộc trước khi cho mượn
>
> Chỉ khi sách có ít nhất một bản sao ở trạng thái **Sẵn sàng** thì mới cho mượn được. Nếu bỏ
> qua bước này, đến bước **Cho mượn** hệ thống sẽ báo không có bản sao sẵn sàng.
>
> Trong menu **...** của mỗi bản sao còn có **Đổi trạng thái** (Sẵn sàng / Mất / Hỏng / Bảo trì)
> và **Xóa**.

### Bước 5 [LÕI]: Thêm độc giả

1. Sidebar → **Thư viện → Độc giả** (`/members`).
2. Bấm **Thêm độc giả**.
3. Điền:
   - **Họ tên** (bắt buộc): `Trần Văn An`.
   - **Email** (bắt buộc, **duy nhất**): `demo01@thuvien.vn` (nhớ đổi mỗi lần chạy lại).
   - **Điện thoại**, **Địa chỉ**: không bắt buộc.
   - **Loại thẻ** (bắt buộc): chọn `Sinh viên` (các lựa chọn: Thường / Sinh viên / Premium).
   - **Ngày hết hạn**: không bắt buộc.
4. Bấm **Tạo**. Thông báo hiện kèm **mã thẻ tự sinh**, ví dụ "Đã tạo độc giả, mã thẻ TV-1000".

> Mỗi loại thẻ có giới hạn mượn riêng (số sách tối đa, số ngày mượn, số lần gia hạn), cấu hình
> tại **Cấu hình** (xem Bước 13).

### Bước 6 [LÕI]: Cho mượn sách

1. Sidebar → **Lưu thông → Mượn / Trả** (`/circulation`), đang ở tab **Cho mượn**.
2. Ô **Độc giả**: gõ tên hoặc mã thẻ (`Trần Văn An`) rồi chọn.
3. Ô **Mã vạch bản sao**: gõ (hoặc quét) `DEMO-01`.
4. Bấm **Cho mượn**. Thông báo "Đã cho mượn" và khối kết quả hiện ra: **Mã phiếu**, tên sách,
   độc giả, và **Hạn trả** được hệ thống tự tính theo chính sách của loại thẻ.

> Ô chọn độc giả chỉ hiển thị độc giả đang **Hoạt động**. Độc giả bị **Tạm khóa / Hết hạn**,
> hoặc đang nợ phạt vượt **Ngưỡng phạt chặn mượn**, sẽ không mượn được.

### Bước 7 [LÕI]: Xem phiếu mượn và gia hạn

1. Sidebar → **Lưu thông → Phiếu mượn** (`/loans`). Phiếu vừa tạo hiện ở đầu bảng, trạng thái
   **Đang mượn**.
2. Có thể tìm theo mã phiếu, lọc theo trạng thái (Đang mượn / Đã trả / Quá hạn), lọc khoảng
   ngày **Từ** - **Đến**.
3. Ở cuối dòng phiếu, mở menu **...**:
   - **Gia hạn**: đẩy hạn trả ra xa và tăng số lần gia hạn (bị giới hạn bởi chính sách loại thẻ).
     Thông báo "Đã gia hạn".
   - **Trả sách**: mở hộp thoại trả (xem Bước 8, cách A).

### Bước 8 [LÕI]: Trả sách

Có hai cách, chọn cách nào cũng được:

**Cách A - từ Phiếu mượn:** menu **...** của phiếu → **Trả sách**. Trong hộp thoại chọn
**Tình trạng sách** (Bình thường / Mất / Hỏng). Nếu chọn **Mất** hoặc **Hỏng** sẽ hiện thêm ô
**Phí thay thế (VND)** (để trống thì dùng phí mặc định trong Cấu hình). Điền **Ghi chú** nếu
cần rồi bấm **Trả sách**.

**Cách B - từ màn hình Mượn / Trả:** mở **Lưu thông → Mượn / Trả**, sang tab **Trả sách**,
nhập **Mã vạch bản sao** `DEMO-01`, chọn **Tình trạng sách**, bấm **Trả sách**. Khối kết quả
hiện ngày trả và cảnh báo nếu trả trễ hạn.

> Sau khi trả với tình trạng **Bình thường**, bản sao quay lại **Sẵn sàng** và có thể cho mượn
> tiếp. Nếu trả với tình trạng **Mất** hoặc **Hỏng**, hệ thống tạo một **khoản phạt phí thay
> thế** cho độc giả (thấy ở màn hình **Phạt**).

### Bước 9 [TÙY CHỌN]: Đặt trước

1. Sidebar → **Lưu thông → Đặt trước** (`/reservations`).
2. Bấm **Thêm đặt trước**, chọn **Độc giả** và **Sách**, bấm **Tạo**.
3. Dùng khi sách đã hết bản sao sẵn sàng và độc giả muốn giữ chỗ. Khi có bản sao trả về, đặt
   trước chuyển sang **Sẵn sàng nhận** kèm **Hạn nhận**. Menu **...** có **Hủy**.
   Bộ lọc trạng thái: Chờ / Sẵn sàng nhận / Đã nhận / Đã hủy / Hết hạn.

### Bước 10 [TÙY CHỌN]: Phạt

1. Sidebar → **Lưu thông → Phạt** (`/fines`).
2. **Cách tạo khoản phạt để demo trực tiếp** (dữ liệu mới chưa có phiếu quá hạn nên nên dùng
   một trong hai cách sau):
   - **Trả sách ở tình trạng Mất/Hỏng** (Bước 8) sẽ tự sinh phạt phí thay thế; hoặc
   - Bấm **Tạo phạt** để tạo thủ công: chọn **Độc giả**, **Loại phạt** (Quá hạn / Mất / Hỏng),
     **Số tiền (VND)**, **Lý do** rồi bấm **Tạo**.
3. Với dòng phạt **Chưa thu**, mở menu **...**:
   - **Thu phạt**: xác nhận đã thu tiền (chuyển sang **Đã thu**).
   - **Miễn phạt**: nhập **Lý do miễn** rồi xác nhận (chuyển sang **Đã miễn**).

> Phạt **Quá hạn** được hệ thống tính tự động khi trả một phiếu đã quá hạn. Trong buổi demo với
> dữ liệu mới (hạn trả còn ở tương lai) sẽ chưa có phạt quá hạn, nên hãy demo phạt bằng hai
> cách ở trên.

### Bước 11 [TÙY CHỌN]: Xem hồ sơ độc giả

1. Vào **Độc giả** (`/members`), bấm vào một dòng độc giả (hoặc menu **...** → **Xem hồ sơ**)
   để mở `/members/{id}`.
2. Trang hồ sơ tổng hợp: **Thông tin thẻ**, **Đang mượn**, **Phạt chưa thu** (kèm tổng số tiền),
   **Đặt trước đang chờ**. Rất hợp để chốt phần demo vì gom mọi hoạt động của độc giả về một chỗ.

### Bước 12 [TÙY CHỌN]: Báo cáo

1. Sidebar → **Quản trị → Báo cáo** (`/reports`).
2. Chọn khoảng thời gian **Từ ngày** - **Đến ngày** (mặc định 30 ngày gần nhất).
3. Xem: 6 thẻ số liệu, **Tổng hợp phạt** (Đã thu / Đã miễn / Chưa thu), biểu đồ **Lượt mượn
   theo thời gian**, **Tồn kho theo tình trạng**, **Sách mượn nhiều nhất**, **Độc giả tích cực**.
4. Nút **Xuất CSV phiếu mượn** và **Xuất CSV phạt** để tải file CSV theo khoảng ngày đã chọn.

### Bước 13 [TÙY CHỌN]: Cấu hình

1. Sidebar → **Quản trị → Cấu hình** (`/settings`).
2. Thẻ **Thông tin thư viện & phạt**: **Tên thư viện**, **Địa chỉ**, **Phí phạt quá hạn mỗi
   ngày (VND)**, **Ngưỡng phạt chặn mượn (VND)**, **Số ngày giữ chỗ đặt trước**, **Phí mặc định
   khi mất sách (VND)**, **Phí mặc định khi hỏng sách (VND)**. Sửa xong bấm **Lưu thay đổi**.
3. Thẻ **Chính sách mượn theo loại thẻ**: mỗi loại thẻ (Thường / Sinh viên / Premium) có
   **Số sách tối đa**, **Số ngày mượn**, **Số lần gia hạn**. Bấm **Sửa** ở từng dòng để chỉnh.

### Bước 14: Tìm kiếm nhanh và đăng xuất

- **Tìm kiếm nhanh**: bấm ô **Tìm kiếm...** trên thanh trên cùng hoặc nhấn `Ctrl + K`, gõ từ 2
  ký tự trở lên để tìm nhanh **Sách**, **Độc giả** và **điều hướng** tới các trang.
- **Đăng xuất**: bấm nút tài khoản ở góc phải trên cùng → chọn đăng xuất (Sign out).

---

## 5. Đường demo lõi cho 7 phút (bản rút gọn)

Chuỗi tối thiểu vẫn thể hiện đủ nghiệp vụ chính. Liếc bảng này khi đang nói:

1. **Đăng nhập** → xem **Bảng điều khiển**.
2. **Tác giả** → **Thêm tác giả** (`Nguyễn Nhật Ánh`).
3. **Danh mục sách** → **Thêm sách** (ISBN `DEMO-978-01`, thể loại `Văn học`, chọn tác giả vừa tạo).
4. Mở sách → **Tải ảnh bìa** → **Thêm bản sao** (mã vạch `DEMO-01`).
5. **Độc giả** → **Thêm độc giả** (email mới, loại thẻ `Sinh viên`).
6. **Mượn / Trả** → tab **Cho mượn** (chọn độc giả + mã vạch `DEMO-01`) → chỉ vào **Hạn trả** tự tính.
7. **Phiếu mượn** → menu **...** → **Gia hạn**.
8. **Mượn / Trả** → tab **Trả sách** (mã vạch `DEMO-01`, tình trạng **Hỏng**, nhập phí) → sinh phạt.
9. **Phạt** → menu **...** → **Thu phạt**.
10. **Báo cáo** → xem biểu đồ, bấm **Xuất CSV phiếu mượn**.

> Nếu chọn cách chuẩn bị dữ liệu trước (an toàn nhất), buổi live chỉ cần chạy các bước 6 → 10.

---

## 6. Bảng tra cứu nhanh các màn hình

| Màn hình | Đường dẫn | Chức năng chính |
| --- | --- | --- |
| Bảng điều khiển | `/` | Số liệu tổng quan, sách quá hạn, sách mượn nhiều |
| Danh mục sách | `/books` | Danh sách sách, thêm/sửa/xóa sách, tìm và lọc, xem lưới/danh sách |
| Chi tiết sách | `/books/{id}` | Thông tin sách, **tải ảnh bìa**, quản lý **bản sao (mã vạch)** |
| Độc giả | `/members` | Danh sách độc giả, thêm/sửa, đổi trạng thái thẻ |
| Hồ sơ độc giả | `/members/{id}` | Thông tin thẻ, đang mượn, phạt chưa thu, đặt trước |
| Mượn / Trả | `/circulation` | Cho mượn theo mã vạch, trả sách theo mã vạch |
| Phiếu mượn | `/loans` | Theo dõi phiếu, gia hạn, trả sách, lọc theo trạng thái/ngày |
| Đặt trước | `/reservations` | Tạo và hủy yêu cầu đặt trước |
| Phạt | `/fines` | Tạo phạt, thu phạt, miễn phạt, lọc theo loại/trạng thái |
| Thể loại | `/categories` | Quản lý thể loại (có phân cấp cha con) |
| Tác giả | `/authors` | Quản lý tác giả |
| Nhà xuất bản | `/publishers` | Quản lý nhà xuất bản |
| Báo cáo | `/reports` | Thống kê, biểu đồ, xuất CSV |
| Cấu hình | `/settings` | Thông tin thư viện, phí phạt, chính sách mượn theo loại thẻ |

---

## 7. Lỗi thường gặp khi demo và cách xử lý

| Hiện tượng | Nguyên nhân | Cách xử lý |
| --- | --- | --- |
| Báo trùng khi **Thêm sách / Thêm bản sao / Thêm độc giả** | ISBN, mã vạch hoặc email đã tồn tại (chạy lại lần 2) | Đổi sang giá trị mới (ISBN/mã vạch/email khác) |
| **Cho mượn** không được, báo không có bản sao | Sách chưa có bản sao, hoặc bản sao đang được mượn / không ở trạng thái Sẵn sàng | Thêm bản sao (Bước 4), hoặc trả bản sao đang mượn trước |
| Không chọn được độc giả khi cho mượn | Độc giả không **Hoạt động**, hoặc nợ phạt vượt ngưỡng chặn mượn | Đổi trạng thái thẻ về **Hoạt động** (ở `/members`) hoặc thu/miễn phạt trước |
| **Gia hạn** báo lỗi | Đã đạt số lần gia hạn tối đa của loại thẻ | Xem/chỉnh **Số lần gia hạn** trong **Cấu hình** |
| Chưa có phạt **Quá hạn** để demo | Dữ liệu mới, hạn trả còn ở tương lai | Demo phạt bằng cách trả **Mất/Hỏng** hoặc **Tạo phạt** thủ công |
| Bị đưa về `/sign-in` | Chưa đăng nhập hoặc phiên hết hạn | Đăng nhập lại bằng tài khoản Clerk |

> Mẹo chốt: chạy hết chuỗi rồi quay lại **Bảng điều khiển** và **Báo cáo** để khán giả thấy số
> liệu vừa thay đổi theo đúng thao tác mình vừa làm. Đó là điểm nhấn thuyết phục nhất.
