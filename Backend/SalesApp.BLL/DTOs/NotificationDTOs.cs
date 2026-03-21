using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class NotificationItemDto
    {
        public int NotificationId { get; set; }
        public string? Message { get; set; }
        public bool IsRead { get; set; }
        public DateTime? CreatedAt { get; set; }
    }

    public class NotificationListResponseDto
    {
        public int Total { get; set; }
        public int UnreadCount { get; set; }
        public IEnumerable<NotificationItemDto> Items { get; set; }
    }

    public class CreateNotificationDto
    {
        [Required]
        public int UserId { get; set; }

        [Required]
        public string Message { get; set; }
    }

    public class NotificationBadgeDto
    {
        public int UnreadNotifications { get; set; }
        public int CartItemCount { get; set; }
    }
}
