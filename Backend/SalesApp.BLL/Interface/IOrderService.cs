using SalesApp.BLL.DTOs;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IOrderService
    {
        Task<CreateOrderResponseDto> CreateAsync(ClaimsPrincipal userPrincipal, CreateOrderDto request, CancellationToken cancellationToken = default);
        Task<OrderListResponseDto> GetMyOrdersAsync(ClaimsPrincipal userPrincipal, int skip = 0, int take = 20, CancellationToken cancellationToken = default);
        Task<OrderDetailDto?> GetByIdAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default);
        Task<BasicResponse> CancelAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default);
        Task<BasicResponse> ConfirmAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default);
        Task<BasicResponse> CompleteAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default);
    }
}
