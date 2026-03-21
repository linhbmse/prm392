using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using SalesApp.BLL.Interfaces;
using System;
using System.Linq;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class NotificationService : INotificationService
    {
        private readonly AppDbContext _context;

        public NotificationService(AppDbContext context)
        {
            _context = context;
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

        public async Task<NotificationListResponseDto> GetMyNotificationsAsync(ClaimsPrincipal userPrincipal, int skip = 0, int take = 20, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var query = _context.Notifications
                .AsNoTracking()
                .Where(n => n.UserId == userId)
                .OrderByDescending(n => n.CreatedAt);

            var total = await query.CountAsync(cancellationToken);
            var unreadCount = await query.CountAsync(n => n.IsRead == false || n.IsRead == null, cancellationToken);

            var items = await query
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(n => new NotificationItemDto
                {
                    NotificationId = n.NotificationId,
                    Message = n.Message,
                    IsRead = n.IsRead ?? false,
                    CreatedAt = n.CreatedAt
                })
                .ToListAsync(cancellationToken);

            return new NotificationListResponseDto
            {
                Total = total,
                UnreadCount = unreadCount,
                Items = items
            };
        }

        public async Task<BasicResponse> MarkReadAsync(ClaimsPrincipal userPrincipal, int notificationId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var notification = await _context.Notifications
                .FirstOrDefaultAsync(n => n.NotificationId == notificationId && n.UserId == userId, cancellationToken);

            if (notification == null) return new BasicResponse(false, "Không tìm thấy thông báo.");

            if (notification.IsRead != true)
            {
                notification.IsRead = true;
                _context.Notifications.Update(notification);
                await _context.SaveChangesAsync(cancellationToken);
            }

            return new BasicResponse(true, "Đã đánh dấu đã đọc.");
        }

        public async Task<BasicResponse> MarkAllReadAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var notifications = await _context.Notifications
                .Where(n => n.UserId == userId && (n.IsRead == false || n.IsRead == null))
                .ToListAsync(cancellationToken);

            if (notifications.Count == 0) return new BasicResponse(true, "Không có thông báo chưa đọc.");

            foreach (var n in notifications)
            {
                n.IsRead = true;
                _context.Notifications.Update(n);
            }
            await _context.SaveChangesAsync(cancellationToken);

            return new BasicResponse(true, "Đã đánh dấu tất cả đã đọc.");
        }

        public async Task<BasicResponse> CreateAsync(CreateNotificationDto request, CancellationToken cancellationToken = default)
        {
            if (request.UserId <= 0) return new BasicResponse(false, "User id không hợp lệ.");
            if (string.IsNullOrWhiteSpace(request.Message)) return new BasicResponse(false, "Nội dung là bắt buộc.");

            var notification = new Notification
            {
                UserId = request.UserId,
                Message = request.Message.Trim(),
                IsRead = false,
                CreatedAt = DateTime.UtcNow
            };

            await _context.Notifications.AddAsync(notification, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            return new BasicResponse(true, "Đã tạo thông báo.");
        }

        public async Task<NotificationBadgeDto> GetBadgeAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);

            var unreadCount = await _context.Notifications
                .CountAsync(n => n.UserId == userId && (n.IsRead == false || n.IsRead == null), cancellationToken);

            var cartItemCount = await _context.CartItems
                .CountAsync(ci => ci.Cart != null && ci.Cart.UserId == userId && ci.Cart.Status == "Active", cancellationToken);

            return new NotificationBadgeDto
            {
                UnreadNotifications = unreadCount,
                CartItemCount = cartItemCount
            };
        }
    }
}
