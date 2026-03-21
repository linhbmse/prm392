using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize] // Requires user to be logged in to access cart
    public class CartController : ControllerBase
    {
        private readonly ICartService _cartService;

        public CartController(ICartService cartService)
        {
            _cartService = cartService;
        }

        [HttpGet]
        public async Task<IActionResult> GetCart(CancellationToken cancellationToken = default)
        {
            try
            {
                var result = await _cartService.GetCartAsync(User, cancellationToken);
                return Ok(result);
            }
            catch (UnauthorizedAccessException ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
        }

        [HttpPost("items")]
        public async Task<IActionResult> AddItem([FromBody] AddCartItemRequestDto request, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            try
            {
                var result = await _cartService.AddItemAsync(User, request, cancellationToken);
                return Ok(result);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (UnauthorizedAccessException ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
        }

        [HttpPut("items/{id}")]
        public async Task<IActionResult> UpdateItem(int id, [FromBody] UpdateCartItemRequestDto request, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            var result = await _cartService.UpdateItemAsync(User, id, request, cancellationToken);
            if (!result) return NotFound(new { message = "Item not found in your cart." });
            
            return Ok(new { success = true, message = "Item updated successfully." });
        }

        [HttpDelete("items/{id}")]
        public async Task<IActionResult> RemoveItem(int id, CancellationToken cancellationToken = default)
        {
            var result = await _cartService.RemoveItemAsync(User, id, cancellationToken);
            if (!result) return NotFound(new { message = "Item not found in your cart." });
            
            return Ok(new { success = true, message = "Item removed successfully." });
        }

        [HttpDelete("clear")]
        public async Task<IActionResult> ClearCart(CancellationToken cancellationToken = default)
        {
            var result = await _cartService.ClearAsync(User, cancellationToken);
            return Ok(new { success = true, message = "Cart cleared successfully." });
        }
    }
}
