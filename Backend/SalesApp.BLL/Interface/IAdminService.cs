using SalesApp.BLL.DTOs;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IAdminService
    {
        // Users
        Task<AdminUserListResponseDto> GetUsersAsync(string? search, string? role, int skip, int take, CancellationToken cancellationToken = default);
        Task<AdminUserListDto?> GetUserByIdAsync(int userId, CancellationToken cancellationToken = default);
        Task<AdminUserListDto?> UpdateUserAsync(int userId, AdminUpdateUserDto request, CancellationToken cancellationToken = default);
        Task<bool> DeleteUserAsync(int userId, CancellationToken cancellationToken = default);

        // Orders
        Task<AdminOrderListResponseDto> GetOrdersAsync(string? status, int? userId, int skip, int take, CancellationToken cancellationToken = default);
        Task<OrderDetailDto?> GetOrderByIdAsync(int orderId, CancellationToken cancellationToken = default);
        Task<BasicResponse> UpdateOrderStatusAsync(int orderId, AdminUpdateOrderStatusDto request, CancellationToken cancellationToken = default);

        // Products
        Task<AdminProductListResponseDto> GetProductsAsync(string? search, int? categoryId, int skip, int take, CancellationToken cancellationToken = default);
        Task<bool> DeleteProductAsync(int productId, CancellationToken cancellationToken = default);

        // Dashboard
        Task<object> GetDashboardAsync(CancellationToken cancellationToken = default);
    }
}
