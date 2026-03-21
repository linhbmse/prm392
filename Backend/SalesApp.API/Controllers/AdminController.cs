using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.API.Controllers
{
    [Route("api/admin")]
    [ApiController]
    [Authorize(Roles = "Admin")]
    public class AdminController : ControllerBase
    {
        private readonly IAdminService _adminService;

        public AdminController(IAdminService adminService)
        {
            _adminService = adminService;
        }

        // ========== DASHBOARD ==========

        [HttpGet("dashboard")]
        public async Task<IActionResult> GetDashboard(CancellationToken cancellationToken = default)
        {
            var result = await _adminService.GetDashboardAsync(cancellationToken);
            return Ok(result);
        }

        // ========== USERS ==========

        [HttpGet("users")]
        public async Task<IActionResult> GetUsers([FromQuery] string? search, [FromQuery] string? role, [FromQuery] int skip = 0, [FromQuery] int take = 20, CancellationToken cancellationToken = default)
        {
            var result = await _adminService.GetUsersAsync(search, role, skip, take, cancellationToken);
            return Ok(result);
        }

        [HttpGet("users/{id}")]
        public async Task<IActionResult> GetUserById(int id, CancellationToken cancellationToken = default)
        {
            var result = await _adminService.GetUserByIdAsync(id, cancellationToken);
            if (result == null) return NotFound(new { message = "Không tìm thấy người dùng." });
            return Ok(result);
        }

        [HttpPut("users/{id}")]
        public async Task<IActionResult> UpdateUser(int id, [FromBody] AdminUpdateUserDto request, CancellationToken cancellationToken = default)
        {
            try
            {
                var result = await _adminService.UpdateUserAsync(id, request, cancellationToken);
                if (result == null) return NotFound(new { message = "Không tìm thấy người dùng." });
                return Ok(result);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpDelete("users/{id}")]
        public async Task<IActionResult> DeleteUser(int id, CancellationToken cancellationToken = default)
        {
            var deleted = await _adminService.DeleteUserAsync(id, cancellationToken);
            if (!deleted) return NotFound(new { message = "Không tìm thấy người dùng." });
            return Ok(new { success = true, message = "Đã xóa người dùng." });
        }

        // ========== ORDERS ==========

        [HttpGet("orders")]
        public async Task<IActionResult> GetOrders([FromQuery] string? status, [FromQuery] int? userId, [FromQuery] int skip = 0, [FromQuery] int take = 20, CancellationToken cancellationToken = default)
        {
            var result = await _adminService.GetOrdersAsync(status, userId, skip, take, cancellationToken);
            return Ok(result);
        }

        [HttpGet("orders/{id}")]
        public async Task<IActionResult> GetOrderById(int id, CancellationToken cancellationToken = default)
        {
            var result = await _adminService.GetOrderByIdAsync(id, cancellationToken);
            if (result == null) return NotFound(new { message = "Không tìm thấy đơn hàng." });
            return Ok(result);
        }

        [HttpPut("orders/{id}/status")]
        public async Task<IActionResult> UpdateOrderStatus(int id, [FromBody] AdminUpdateOrderStatusDto request, CancellationToken cancellationToken = default)
        {
            var result = await _adminService.UpdateOrderStatusAsync(id, request, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return Ok(new { success = true, message = result.Message });
        }

        // ========== PRODUCTS ==========

        [HttpGet("products")]
        public async Task<IActionResult> GetProducts([FromQuery] string? search, [FromQuery] int? categoryId, [FromQuery] int skip = 0, [FromQuery] int take = 20, CancellationToken cancellationToken = default)
        {
            var result = await _adminService.GetProductsAsync(search, categoryId, skip, take, cancellationToken);
            return Ok(result);
        }

        [HttpDelete("products/{id}")]
        public async Task<IActionResult> DeleteProduct(int id, CancellationToken cancellationToken = default)
        {
            var deleted = await _adminService.DeleteProductAsync(id, cancellationToken);
            if (!deleted) return NotFound(new { message = "Không tìm thấy sản phẩm." });
            return Ok(new { success = true, message = "Đã xóa sản phẩm." });
        }

        // ========== CATEGORIES (reuses existing CategoryService) ==========
        // Admin can use existing /api/Category endpoints with [Authorize] already added 

        // ========== NOTIFICATIONS ==========

        [HttpPost("notifications")]
        public async Task<IActionResult> CreateNotification([FromBody] CreateNotificationDto request, [FromServices] INotificationService notificationService, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);
            var result = await notificationService.CreateAsync(request, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return StatusCode(201, new { success = true, message = result.Message });
        }
    }
}
