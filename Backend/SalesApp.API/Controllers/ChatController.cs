using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class ChatController : ControllerBase
    {
        private readonly IChatService _chatService;

        public ChatController(IChatService chatService)
        {
            _chatService = chatService;
        }

        /// <summary>
        /// Get all chat conversations (Admin only).
        /// Returns a list of users the admin has chatted with.
        /// </summary>
        [HttpGet("conversations")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> GetConversations(CancellationToken cancellationToken = default)
        {
            var result = await _chatService.GetConversationsAsync(User, cancellationToken);
            return Ok(result);
        }

        /// <summary>
        /// Get chat message history. 
        /// Pass otherUserId to filter conversation with a specific user.
        /// </summary>
        [HttpGet("messages")]
        public async Task<IActionResult> GetMessages(
            [FromQuery] int? otherUserId,
            [FromQuery] int skip = 0,
            [FromQuery] int take = 50,
            CancellationToken cancellationToken = default)
        {
            var result = await _chatService.GetMessagesAsync(User, otherUserId, skip, take, cancellationToken);
            return Ok(result);
        }

        /// <summary>
        /// Send a message via REST (alternative to SignalR hub).
        /// For real-time, prefer connecting via SignalR at /hubs/chat.
        /// </summary>
        [HttpPost("messages")]
        public async Task<IActionResult> SendMessage([FromBody] SendMessageDto request, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);
            var result = await _chatService.SendMessageAsync(User, request, cancellationToken);
            return Ok(result);
        }
    }
}
