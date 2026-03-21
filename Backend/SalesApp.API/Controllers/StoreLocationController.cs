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
    public class StoreLocationController : ControllerBase
    {
        private readonly IStoreLocationService _storeLocationService;

        public StoreLocationController(IStoreLocationService storeLocationService)
        {
            _storeLocationService = storeLocationService;
        }

        /// <summary>
        /// Get all store locations (public - for Map Screen)
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetAll(CancellationToken cancellationToken = default)
        {
            var result = await _storeLocationService.GetAllAsync(cancellationToken);
            return Ok(result);
        }

        /// <summary>
        /// Get a store location by ID
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id, CancellationToken cancellationToken = default)
        {
            var result = await _storeLocationService.GetByIdAsync(id, cancellationToken);
            if (result == null) return NotFound(new { message = "Không tìm thấy vị trí cửa hàng." });
            return Ok(result);
        }

        /// <summary>
        /// Create a new store location (admin)
        /// </summary>
        [HttpPost]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Create([FromBody] CreateStoreLocationDto request, CancellationToken cancellationToken = default)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);
            var result = await _storeLocationService.CreateAsync(request, cancellationToken);
            return CreatedAtAction(nameof(GetById), new { id = result.LocationId }, result);
        }

        /// <summary>
        /// Update a store location (admin)
        /// </summary>
        [HttpPut("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Update(int id, [FromBody] UpdateStoreLocationDto request, CancellationToken cancellationToken = default)
        {
            var result = await _storeLocationService.UpdateAsync(id, request, cancellationToken);
            if (result == null) return NotFound(new { message = "Không tìm thấy vị trí cửa hàng." });
            return Ok(result);
        }

        /// <summary>
        /// Delete a store location (admin)
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Delete(int id, CancellationToken cancellationToken = default)
        {
            var deleted = await _storeLocationService.DeleteAsync(id, cancellationToken);
            if (!deleted) return NotFound(new { message = "Không tìm thấy vị trí cửa hàng." });
            return Ok(new { success = true, message = "Đã xóa vị trí cửa hàng." });
        }
    }
}
