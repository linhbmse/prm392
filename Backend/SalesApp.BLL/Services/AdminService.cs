using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class AdminService : IAdminService
    {
        private readonly AppDbContext _context;

        public AdminService(AppDbContext context)
        {
            _context = context;
        }

        // ========== USERS ==========

        public async Task<AdminUserListResponseDto> GetUsersAsync(string? search, string? role, int skip, int take, CancellationToken cancellationToken = default)
        {
            var query = _context.Users.AsNoTracking().AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                query = query.Where(u =>
                    u.Username.Contains(search) ||
                    u.Email.Contains(search) ||
                    (u.PhoneNumber != null && u.PhoneNumber.Contains(search)));
            }
            if (!string.IsNullOrWhiteSpace(role))
            {
                query = query.Where(u => u.Role == role);
            }

            var total = await query.CountAsync(cancellationToken);
            var items = await query
                .OrderByDescending(u => u.UserId)
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(u => new AdminUserListDto
                {
                    UserId = u.UserId,
                    Username = u.Username,
                    Email = u.Email,
                    PhoneNumber = u.PhoneNumber,
                    Address = u.Address,
                    Role = u.Role
                })
                .ToListAsync(cancellationToken);

            return new AdminUserListResponseDto { Total = total, Items = items };
        }

        public async Task<AdminUserListDto?> GetUserByIdAsync(int userId, CancellationToken cancellationToken = default)
        {
            var user = await _context.Users.FindAsync(new object[] { userId }, cancellationToken);
            if (user == null) return null;

            return new AdminUserListDto
            {
                UserId = user.UserId,
                Username = user.Username,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                Address = user.Address,
                Role = user.Role
            };
        }

        public async Task<AdminUserListDto?> UpdateUserAsync(int userId, AdminUpdateUserDto request, CancellationToken cancellationToken = default)
        {
            var user = await _context.Users.FindAsync(new object[] { userId }, cancellationToken);
            if (user == null) return null;

            if (!string.IsNullOrWhiteSpace(request.Email)) user.Email = request.Email.Trim();
            if (!string.IsNullOrWhiteSpace(request.PhoneNumber)) user.PhoneNumber = request.PhoneNumber.Trim();
            if (!string.IsNullOrWhiteSpace(request.Address)) user.Address = request.Address.Trim();
            if (!string.IsNullOrWhiteSpace(request.Role))
            {
                var normalizedRole = request.Role.Trim();
                if (normalizedRole != "User" && normalizedRole != "Admin")
                {
                    throw new InvalidOperationException("Role không hợp lệ. Chỉ chấp nhận: User, Admin.");
                }
                user.Role = normalizedRole;
            }

            _context.Users.Update(user);
            await _context.SaveChangesAsync(cancellationToken);

            return new AdminUserListDto
            {
                UserId = user.UserId,
                Username = user.Username,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                Address = user.Address,
                Role = user.Role
            };
        }

        public async Task<bool> DeleteUserAsync(int userId, CancellationToken cancellationToken = default)
        {
            var user = await _context.Users.FindAsync(new object[] { userId }, cancellationToken);
            if (user == null) return false;

            _context.Users.Remove(user);
            await _context.SaveChangesAsync(cancellationToken);
            return true;
        }

        // ========== ORDERS ==========

        public async Task<AdminOrderListResponseDto> GetOrdersAsync(string? status, int? userId, int skip, int take, CancellationToken cancellationToken = default)
        {
            var query = _context.Orders.AsNoTracking()
                .Include(o => o.User)
                .Include(o => o.Cart)
                .AsQueryable();

            if (!string.IsNullOrWhiteSpace(status))
            {
                query = query.Where(o => o.OrderStatus == status);
            }
            if (userId.HasValue)
            {
                query = query.Where(o => o.UserId == userId.Value);
            }

            var total = await query.CountAsync(cancellationToken);
            var items = await query
                .OrderByDescending(o => o.OrderDate)
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(o => new AdminOrderListDto
                {
                    OrderId = o.OrderId,
                    UserId = o.UserId,
                    Username = o.User != null ? o.User.Username : null,
                    TotalAmount = o.Cart != null ? o.Cart.TotalPrice : 0,
                    PaymentMethod = o.PaymentMethod,
                    OrderStatus = o.OrderStatus,
                    BillingAddress = o.BillingAddress,
                    OrderDate = o.OrderDate
                })
                .ToListAsync(cancellationToken);

            return new AdminOrderListResponseDto { Total = total, Items = items };
        }

        public async Task<OrderDetailDto?> GetOrderByIdAsync(int orderId, CancellationToken cancellationToken = default)
        {
            var order = await _context.Orders
                .Include(o => o.Cart)
                    .ThenInclude(c => c.CartItems)
                        .ThenInclude(ci => ci.Product)
                .Include(o => o.Payments)
                .FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null) return null;

            var latestPayment = order.Payments.OrderByDescending(p => p.PaymentDate).FirstOrDefault();
            var cartItems = order.Cart?.CartItems?.Select(ci => new OrderItemDto
            {
                ProductId = ci.ProductId ?? 0,
                ProductName = ci.Product != null ? ci.Product.ProductName : "Sản phẩm không tồn tại",
                ImageUrl = ci.Product?.ImageUrl,
                Quantity = ci.Quantity,
                Price = ci.Price
            }) ?? Enumerable.Empty<OrderItemDto>();

            return new OrderDetailDto
            {
                OrderId = order.OrderId,
                CartId = order.CartId,
                UserId = order.UserId ?? 0,
                TotalAmount = order.Cart?.TotalPrice,
                PaymentMethod = order.PaymentMethod,
                OrderStatus = order.OrderStatus,
                BillingAddress = order.BillingAddress,
                OrderDate = order.OrderDate,
                PaymentStatus = latestPayment?.PaymentStatus,
                Items = cartItems
            };
        }

        public async Task<BasicResponse> UpdateOrderStatusAsync(int orderId, AdminUpdateOrderStatusDto request, CancellationToken cancellationToken = default)
        {
            var order = await _context.Orders
                .Include(o => o.Payments)
                .FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null) return new BasicResponse(false, "Không tìm thấy đơn hàng.");

            var allowedStatuses = new[] { "PENDING", "PROCESSING", "CONFIRMED", "COMPLETED", "CANCELLED" };
            if (!allowedStatuses.Contains(request.Status, StringComparer.OrdinalIgnoreCase))
            {
                return new BasicResponse(false, $"Trạng thái không hợp lệ. Cho phép: {string.Join(", ", allowedStatuses)}");
            }

            if (string.Equals(order.OrderStatus, request.Status, StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(true, "Đơn hàng đã ở trạng thái yêu cầu.");
            }

            // Cancel pending payments when cancelling order
            if (string.Equals(request.Status, "CANCELLED", StringComparison.OrdinalIgnoreCase))
            {
                foreach (var payment in order.Payments.Where(p => string.Equals(p.PaymentStatus, "PENDING", StringComparison.OrdinalIgnoreCase)))
                {
                    payment.PaymentStatus = "CANCELLED";
                    _context.Payments.Update(payment);
                }
            }

            order.OrderStatus = request.Status.ToUpper();
            _context.Orders.Update(order);
            await _context.SaveChangesAsync(cancellationToken);

            return new BasicResponse(true, $"Đã cập nhật trạng thái đơn hàng thành {request.Status}.");
        }

        // ========== PRODUCTS ==========

        public async Task<AdminProductListResponseDto> GetProductsAsync(string? search, int? categoryId, int skip, int take, CancellationToken cancellationToken = default)
        {
            var query = _context.Products.AsNoTracking()
                .Include(p => p.Category)
                .AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                query = query.Where(p => p.ProductName.Contains(search));
            }
            if (categoryId.HasValue)
            {
                query = query.Where(p => p.CategoryId == categoryId.Value);
            }

            var total = await query.CountAsync(cancellationToken);
            var items = await query
                .OrderByDescending(p => p.ProductId)
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(p => new AdminProductListDto
                {
                    ProductId = p.ProductId,
                    ProductName = p.ProductName,
                    Price = p.Price,
                    CategoryId = p.CategoryId,
                    CategoryName = p.Category != null ? p.Category.CategoryName : null,
                    ImageUrl = p.ImageUrl
                })
                .ToListAsync(cancellationToken);

            return new AdminProductListResponseDto { Total = total, Items = items };
        }

        public async Task<bool> DeleteProductAsync(int productId, CancellationToken cancellationToken = default)
        {
            var product = await _context.Products.FindAsync(new object[] { productId }, cancellationToken);
            if (product == null) return false;

            _context.Products.Remove(product);
            await _context.SaveChangesAsync(cancellationToken);
            return true;
        }

        // ========== DASHBOARD ==========

        public async Task<object> GetDashboardAsync(CancellationToken cancellationToken = default)
        {
            var totalUsers = await _context.Users.CountAsync(cancellationToken);
            var totalProducts = await _context.Products.CountAsync(cancellationToken);
            var totalOrders = await _context.Orders.CountAsync(cancellationToken);
            var pendingOrders = await _context.Orders.CountAsync(o => o.OrderStatus == "PENDING", cancellationToken);
            var completedOrders = await _context.Orders.CountAsync(o => o.OrderStatus == "COMPLETED", cancellationToken);
            var totalCategories = await _context.Categories.CountAsync(cancellationToken);

            return new
            {
                TotalUsers = totalUsers,
                TotalProducts = totalProducts,
                TotalOrders = totalOrders,
                PendingOrders = pendingOrders,
                CompletedOrders = completedOrders,
                TotalCategories = totalCategories
            };
        }
    }
}
