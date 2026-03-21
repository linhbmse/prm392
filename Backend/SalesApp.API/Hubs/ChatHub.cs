using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using System;
using System.Security.Claims;
using System.Threading.Tasks;

namespace SalesApp.API.Hubs
{
    [Authorize]
    public class ChatHub : Hub
    {
        private readonly IChatService _chatService;

        public ChatHub(IChatService chatService)
        {
            _chatService = chatService;
        }

        /// <summary>
        /// Called when a client connects. Adds user to their personal group.
        /// </summary>
        public override async Task OnConnectedAsync()
        {
            var userId = GetUserId();
            if (userId > 0)
            {
                await Groups.AddToGroupAsync(Context.ConnectionId, $"user_{userId}");
            }
            await base.OnConnectedAsync();
        }

        public override async Task OnDisconnectedAsync(Exception? exception)
        {
            var userId = GetUserId();
            if (userId > 0)
            {
                await Groups.RemoveFromGroupAsync(Context.ConnectionId, $"user_{userId}");
            }
            await base.OnDisconnectedAsync(exception);
        }

        /// <summary>
        /// Send a message. The message is persisted, then broadcast to both sender and receiver groups.
        /// </summary>
        public async Task SendMessage(string message, int? receiverUserId)
        {
            if (string.IsNullOrWhiteSpace(message)) return;

            var request = new SendMessageDto
            {
                Message = message,
                ReceiverUserId = receiverUserId
            };

            var result = await _chatService.SendMessageAsync(Context.User!, request);

            // Send to the sender
            await Clients.Caller.SendAsync("ReceiveMessage", result);

            // Send to the receiver if specified
            if (receiverUserId.HasValue)
            {
                await Clients.Group($"user_{receiverUserId.Value}").SendAsync("ReceiveMessage", result);
            }
            else
            {
                // Broadcast to all connected clients
                await Clients.Others.SendAsync("ReceiveMessage", result);
            }
        }

        private int GetUserId()
        {
            var idClaim = Context.User?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (!string.IsNullOrEmpty(idClaim) && int.TryParse(idClaim, out int userId))
            {
                return userId;
            }
            return 0;
        }
    }
}
