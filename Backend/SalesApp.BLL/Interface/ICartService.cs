using SalesApp.BLL.DTOs;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface ICartService
    {
        Task<CartResponseDto> GetCartAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default);
        Task<CartItemResponseDto> AddItemAsync(ClaimsPrincipal userPrincipal, AddCartItemRequestDto request, CancellationToken cancellationToken = default);
        Task<bool> UpdateItemAsync(ClaimsPrincipal userPrincipal, int cartItemId, UpdateCartItemRequestDto request, CancellationToken cancellationToken = default);
        Task<bool> RemoveItemAsync(ClaimsPrincipal userPrincipal, int cartItemId, CancellationToken cancellationToken = default);
        Task<bool> ClearAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default);
    }
}
