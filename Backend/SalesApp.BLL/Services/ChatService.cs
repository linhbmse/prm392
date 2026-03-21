using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System;
using System.Linq;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class ChatService : IChatService
    {
        private readonly AppDbContext _context;
        private readonly INotificationService _notificationService;

        public ChatService(AppDbContext context, INotificationService notificationService)
        {
            _context = context;
            _notificationService = notificationService;
        }

        private int GetUserId(ClaimsPrincipal principal)
        {
            var idClaim = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim) || !int.TryParse(idClaim, out int userId))
            {
                throw new UnauthorizedAccessException("Người dùng không hợp lệ.");
            }
            return userId;
        }

        private string GetUserRole(ClaimsPrincipal principal)
        {
            return principal.FindFirst(ClaimTypes.Role)?.Value ?? "User";
        }

        public async Task<ChatHistoryResponseDto> GetMessagesAsync(ClaimsPrincipal userPrincipal, int? otherUserId, int skip = 0, int take = 50, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var role = GetUserRole(userPrincipal);

            IQueryable<ChatMessage> query;

            if (role == "Admin")
            {
                if (otherUserId.HasValue)
                {
                    // Admin xem conversation với 1 user cụ thể
                    query = _context.ChatMessages
                        .Where(m =>
                            (m.UserId == userId && m.ReceiverUserId == otherUserId.Value) ||
                            (m.UserId == otherUserId.Value && m.ReceiverUserId == userId));
                }
                else
                {
                    // Admin xem tất cả tin nhắn gửi cho mình
                    query = _context.ChatMessages
                        .Where(m => m.UserId == userId || m.ReceiverUserId == userId);
                }
            }
            else
            {
                // User bình thường: chỉ thấy conversation của mình với admin
                query = _context.ChatMessages
                    .Where(m => m.UserId == userId || m.ReceiverUserId == userId);
            }

            var total = await query.CountAsync(cancellationToken);
            var messages = await query
                .OrderByDescending(m => m.SentAt)
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 50 : Math.Min(take, 100))
                .Include(m => m.User)
                .Select(m => new ChatMessageDto
                {
                    ChatMessageId = m.ChatMessageId,
                    UserId = m.UserId ?? 0,
                    Username = m.User != null ? m.User.Username : null,
                    UserRole = m.User != null ? m.User.Role : null,
                    ReceiverUserId = m.ReceiverUserId,
                    Message = m.Message,
                    SentAt = m.SentAt
                })
                .ToListAsync(cancellationToken);

            return new ChatHistoryResponseDto
            {
                Total = total,
                Messages = messages
            };
        }

        public async Task<ChatMessageDto> SendMessageAsync(ClaimsPrincipal userPrincipal, SendMessageDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var role = GetUserRole(userPrincipal);

            int? receiverId;

            if (role == "Admin")
            {
                // Admin phải chỉ định receiver khi reply
                if (!request.ReceiverUserId.HasValue)
                {
                    throw new InvalidOperationException("Admin cần chỉ định ReceiverUserId khi trả lời.");
                }
                receiverId = request.ReceiverUserId.Value;
            }
            else
            {
                // User bình thường → auto-route tới Admin đầu tiên
                var admin = await _context.Users
                    .Where(u => u.Role == "Admin")
                    .OrderBy(u => u.UserId)
                    .FirstOrDefaultAsync(cancellationToken);

                if (admin == null)
                {
                    throw new InvalidOperationException("Hiện tại không có nhân viên hỗ trợ. Vui lòng thử lại sau.");
                }
                receiverId = admin.UserId;
            }

            var chatMessage = new ChatMessage
            {
                UserId = userId,
                ReceiverUserId = receiverId,
                Message = request.Message.Trim(),
                SentAt = DateTime.UtcNow
            };

            await _context.ChatMessages.AddAsync(chatMessage, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            // Gửi notification cho người nhận
            if (receiverId.HasValue)
            {
                var senderName = (await _context.Users.FindAsync(new object[] { userId }, cancellationToken))?.Username ?? "Ai đó";
                await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
                {
                    UserId = receiverId.Value,
                    Message = $"Bạn có tin nhắn mới từ {senderName}."
                }, cancellationToken);
            }

            var user = await _context.Users.FindAsync(new object[] { userId }, cancellationToken);

            return new ChatMessageDto
            {
                ChatMessageId = chatMessage.ChatMessageId,
                UserId = userId,
                Username = user?.Username,
                UserRole = user?.Role,
                ReceiverUserId = receiverId,
                Message = chatMessage.Message,
                SentAt = chatMessage.SentAt
            };
        }
    }
}
