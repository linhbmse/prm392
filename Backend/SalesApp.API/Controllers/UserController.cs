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
    [Authorize]
    public class UserController : ControllerBase
    {
        private readonly IUserService _userService;

        public UserController(IUserService userService)
        {
            _userService = userService;
        }

        /// <summary>
        /// Get current user profile
        /// </summary>
        [HttpGet("me")]
        public async Task<IActionResult> GetMe(CancellationToken cancellationToken = default)
        {
            var result = await _userService.GetProfileAsync(User, cancellationToken);
            return Ok(result);
        }

        /// <summary>
        /// Update current user profile (email, phone, address)
        /// </summary>
        [HttpPut("me")]
        public async Task<IActionResult> UpdateMe([FromBody] UpdateProfileDto request, CancellationToken cancellationToken = default)
        {
            try
            {
                var result = await _userService.UpdateProfileAsync(User, request, cancellationToken);
                return Ok(result);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Change password
        /// </summary>
        [HttpPut("me/password")]
        public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordDto request, CancellationToken cancellationToken = default)
        {
            var result = await _userService.ChangePasswordAsync(User, request, cancellationToken);
            if (!result.Success) return BadRequest(new { message = result.Message });
            return Ok(new { success = true, message = result.Message });
        }
    }
}
