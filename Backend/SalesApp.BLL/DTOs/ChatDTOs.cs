using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class ChatMessageDto
    {
        public int ChatMessageId { get; set; }
        public int UserId { get; set; }
        public string? Username { get; set; }
        public string? UserRole { get; set; }
        public int? ReceiverUserId { get; set; }
        public string? Message { get; set; }
        public DateTime? SentAt { get; set; }
    }

    public class ChatConversationDto
    {
        public int UserId { get; set; }
        public string? Username { get; set; }
        public string? UserRole { get; set; }
        public string? LastMessage { get; set; }
        public DateTime? LastMessageAt { get; set; }
    }

    public class ChatHistoryResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<ChatMessageDto> Messages { get; set; }
    }

    public class SendMessageDto
    {
        [Required]
        public string Message { get; set; }

        /// <summary>
        /// Required only for Admin/Staff when replying to a specific user.
        /// For normal users, this is ignored — backend auto-routes to Admin.
        /// </summary>
        public int? ReceiverUserId { get; set; }
    }
}
