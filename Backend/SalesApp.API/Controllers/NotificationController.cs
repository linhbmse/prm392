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
    public class NotificationController : ControllerBase
    {
        private readonly INotificationService _notificationService;

        public NotificationController(INotificationService notificationService)
        {
            _notificationService = notificationService;
        }

        /// <summary>
        /// Get all notifications for the current user (paginated)
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetMyNotifications([FromQuery] int skip = 0, [FromQuery] int take = 20, CancellationToken cancellationToken = default)
        {
            var result = await _notificationService.GetMyNotificationsAsync(User, skip, take, cancellationToken);
            return Ok(result);
        }

        /// <summary>
        /// Get badge counts: unread notifications + cart item count (for Android app badge)
        /// </summary>
        [HttpGet("badge")]
        public async Task<IActionResult> GetBadge(CancellationToken cancellationToken = default)
        {
            var result = await _notificationService.GetBadgeAsync(User, cancellationToken);
            return Ok(result);
        }

        /// <summary>
        /// Mark a single notification as read
        /// </summary>
        [HttpPut("{id}/read")]
        public async Task<IActionResult> MarkRead(int id, CancellationToken cancellationToken = default)
        {
            var result = await _notificationService.MarkReadAsync(User, id, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return Ok(new { success = true, message = result.Message });
        }

        /// <summary>
        /// Mark all notifications as read
        /// </summary>
        [HttpPut("read-all")]
        public async Task<IActionResult> MarkAllRead(CancellationToken cancellationToken = default)
        {
            var result = await _notificationService.MarkAllReadAsync(User, cancellationToken);
            return Ok(new { success = true, message = result.Message });
        }

        /// <summary>
        /// Create a notification (for internal/admin use)
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CreateNotificationDto request, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            var result = await _notificationService.CreateAsync(request, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return StatusCode(201, new { success = true, message = result.Message });
        }
    }
}
