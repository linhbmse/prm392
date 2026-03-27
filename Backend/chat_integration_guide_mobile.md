# Hướng Dẫn Tích Hợp Luồng Chat (Dành cho Mobile)

Tài liệu này hướng dẫn cách tích hợp luồng chat trên ứng dụng Mobile, đặc biệt là bao gồm tính năng dành cho **Admin** (xem danh sách người chat) và tính năng nhắn tin cơ bản.

---

## 1. Dành cho Admin: Lấy danh sách hội thoại
Sử dụng API này để hiển thị danh sách tất cả các User đã từng nhắn tin với Admin hiện tại.

- **Endpoint:** `GET /api/chat/conversations`
- **Auth:** Bearer Token (Yêu cầu tài khoản có Role là `Admin`)
- **Response (200 OK):**
  Trả về một mảng danh sách hội thoại, được sắp xếp theo thời gian tin nhắn mới nhất (giảm dần).

```json
[
  {
    "userId": 10,
    "username": "nguyenvana",
    "userRole": "User",
    "lastMessage": "Cho mình hỏi về sản phẩm này?",
    "lastMessageAt": "2026-03-26T12:00:00Z"
  },
  ...
]
```
- **Xử lý trên Mobile:** Hiển thị danh sách này dưới dạng các item. Khi Admin click vào một item, truyền `userId` tương ứng sang màn hình chi tiết Chat (Bước 2).

---

## 2. Dành cho Cả Admin và User: Lấy chi tiết tin nhắn của 1 phiên Chat
API này dùng để tải lịch sử tin nhắn.

- **Endpoint:** `GET /api/chat/messages?otherUserId={userId}&skip={skip}&take={take}`
  - `otherUserId`: 
    - Đối với **Admin**: truyền `userId` của khách hàng (lấy từ bước 1).
    - Đối với **User thường**: không cần truyền (bỏ qua parameter này, hệ thống sẽ tự động ghép cặp với Admin).
  - `skip`: Số lượng tin nhắn bỏ qua (Dùng để phân trang/load more, mặc định: 0).
  - `take`: Số lượng tin nhắn cần lấy (mặc định: 50, tối đa 100).
- **Auth:** Bearer Token
- **Response (200 OK):**

```json
{
  "total": 150,
  "messages": [
    {
      "chatMessageId": 102,
      "userId": 10,
      "username": "nguyenvana",
      "userRole": "User",
      "receiverUserId": 1, 
      "message": "Cho mình hỏi về sản phẩm này?",
      "sentAt": "2026-03-26T12:00:00Z"
    },
    ...
  ]
}
```

---

## 3. Dành cho Cả Admin và User: Gửi tin nhắn
API RESTful để gửi tin nhắn mới.

- **Endpoint:** `POST /api/chat/messages`
- **Auth:** Bearer Token
- **Body:**

```json
{
  "message": "Chào bạn, mình cần hỗ trợ",
  "receiverUserId": null 
}
```
*Lưu ý về `receiverUserId`:*
- **User thường:** Gửi `null` (hoặc không gửi field này đo). Backend sẽ tự động tìm Admin để nhận tin nhắn.
- **Admin:** BẮT BUỘC phải truyền `receiverUserId` là `userId` của khách hàng đang chat.

- **Response (200 OK):**
Trả về đối tượng [ChatMessageDto](file:///e:/FPT/PRM392/prm392/Backend/SalesApp.BLL/DTOs/ChatDTOs.cs#7-17) vừa gửi thành công.

---

## 4. Gợi ý Tích hợp Real-time (SignalR)
Nếu ứng dụng có hỗ trợ Real-time:
- Mở kết nối SignalR tới `wss://<domain-backend>/hubs/chat` (kèm Bearer Token).
- Lắng nghe event nhận tin nhắn từ Server. 
- *Lưu ý:* Chi tiết mô hình event SignalR sẽ tùy thuộc vào cấu hình Hub hiện hành của Backend (cần thống nhất tên method lắng nghe, ví dụ: `ReceiveMessage`). Nếu chưa thiết lập Real-time, bạn có thể gọi lại API `GET /api/chat/messages` dạng polling (ví dụ: cứ 3-5 giây gọi 1 lần khi đang mở màn hình chat, chỉ dùng khi bất đắc dĩ) hoặc tiếp tục sử dụng REST API.
