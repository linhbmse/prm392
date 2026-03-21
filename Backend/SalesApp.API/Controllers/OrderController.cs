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
    public class OrderController : ControllerBase
    {
        private readonly IOrderService _orderService;

        public OrderController(IOrderService orderService)
        {
            _orderService = orderService;
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CreateOrderDto request, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);
            try
            {
                var result = await _orderService.CreateAsync(User, request, cancellationToken);
                return Ok(result);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet]
        public async Task<IActionResult> GetMyOrders([FromQuery] int skip = 0, [FromQuery] int take = 20, CancellationToken cancellationToken = default)
        {
            var result = await _orderService.GetMyOrdersAsync(User, skip, take, cancellationToken);
            return Ok(result);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id, CancellationToken cancellationToken = default)
        {
            var result = await _orderService.GetByIdAsync(User, id, cancellationToken);
            if (result == null) return NotFound(new { message = "Đơn hàng không tồn tại." });
            return Ok(result);
        }

        [HttpPut("{id}/cancel")]
        public async Task<IActionResult> Cancel(int id, CancellationToken cancellationToken = default)
        {
            var result = await _orderService.CancelAsync(User, id, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return Ok(new { success = true, message = result.Message });
        }

        [HttpPut("{id}/confirm")]
        public async Task<IActionResult> Confirm(int id, CancellationToken cancellationToken = default)
        {
            var result = await _orderService.ConfirmAsync(User, id, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return Ok(new { success = true, message = result.Message });
        }

        [HttpPut("{id}/complete")]
        public async Task<IActionResult> Complete(int id, CancellationToken cancellationToken = default)
        {
            var result = await _orderService.CompleteAsync(User, id, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return Ok(new { success = true, message = result.Message });
        }
    }
}
