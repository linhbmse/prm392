using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System;
using System.Linq;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class OrderService : IOrderService
    {
        private readonly AppDbContext _context;
        private readonly INotificationService _notificationService;

        public OrderService(AppDbContext context, INotificationService notificationService)
        {
            _context = context;
            _notificationService = notificationService;
        }

        private int GetUserId(ClaimsPrincipal principal)
        {
            var idClaim = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim) || !int.TryParse(idClaim, out int userId))
            {
                throw new UnauthorizedAccessException("Người dùng không hợp lệ.");
            }
            return userId;
        }

        public async Task<CreateOrderResponseDto> CreateAsync(ClaimsPrincipal userPrincipal, CreateOrderDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var method = request.PaymentMethod?.Trim().ToUpper();
            if (method != "PAYOS" && method != "COD")
            {
                throw new InvalidOperationException("Phương thức thanh toán không được hỗ trợ. Chỉ chấp nhận PAYOS hoặc COD.");
            }

            var cart = await _context.Carts
                .Include(c => c.CartItems)
                .FirstOrDefaultAsync(c => c.UserId == userId && c.Status == "Active", cancellationToken);

            if (cart == null || cart.CartItems.Count == 0 || cart.TotalPrice <= 0)
            {
                throw new InvalidOperationException("Giỏ hàng trống.");
            }

            var order = new Order
            {
                UserId = userId,
                CartId = cart.CartId,
                PaymentMethod = method,
                BillingAddress = request.BillingAddress.Trim(),
                OrderStatus = "PENDING",
                OrderDate = DateTime.UtcNow
            };

            await _context.Orders.AddAsync(order, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            cart.Status = "Completed";
            _context.Carts.Update(cart);
            await _context.SaveChangesAsync(cancellationToken);

            // Gửi notification cho user
            await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
            {
                UserId = userId,
                Message = $"Đơn hàng #{order.OrderId} đã được tạo thành công. Phương thức: {method}."
            }, cancellationToken);

            return new CreateOrderResponseDto
            {
                OrderId = order.OrderId,
                TotalAmount = cart.TotalPrice,
                PaymentMethod = order.PaymentMethod,
                OrderStatus = order.OrderStatus,
                BillingAddress = order.BillingAddress,
                OrderDate = order.OrderDate
            };
        }

        public async Task<OrderListResponseDto> GetMyOrdersAsync(ClaimsPrincipal userPrincipal, int skip = 0, int take = 20, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var query = _context.Orders
                .Where(o => o.UserId == userId);

            var total = await query.CountAsync(cancellationToken);
            var items = await query
                .OrderByDescending(o => o.OrderDate)
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(o => new OrderListItemDto
                {
                    OrderId = o.OrderId,
                    UserId = o.UserId ?? 0,
                    TotalAmount = o.Cart != null ? o.Cart.TotalPrice : 0,
                    PaymentMethod = o.PaymentMethod,
                    OrderStatus = o.OrderStatus,
                    BillingAddress = o.BillingAddress,
                    OrderDate = o.OrderDate
                })
                .ToListAsync(cancellationToken);

            return new OrderListResponseDto
            {
                Total = total,
                Items = items
            };
        }

        public async Task<OrderDetailDto?> GetByIdAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders
                .Include(o => o.Cart)
                    .ThenInclude(c => c.CartItems)
                        .ThenInclude(ci => ci.Product)
                .Include(o => o.Payments)
                .FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null) return null;
            if (order.UserId != userId) return null;

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

        public async Task<BasicResponse> CancelAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders
                .Include(o => o.Payments)
                .FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null) return new BasicResponse(false, "Không tìm thấy đơn hàng.");
            if (order.UserId != userId) return new BasicResponse(false, "Không có quyền thực hiện.");

            if (string.Equals(order.OrderStatus, "CANCELLED", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(true, "Đơn hàng đã bị hủy.");
            }

            if (!string.Equals(order.OrderStatus, "PENDING", StringComparison.OrdinalIgnoreCase) &&
                !string.Equals(order.OrderStatus, "PROCESSING", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(false, "Chỉ đơn hàng đang chờ hoặc đang xử lý mới có thể hủy.");
            }

            order.OrderStatus = "CANCELLED";
            _context.Orders.Update(order);

            // Cancel pending payments
            foreach (var payment in order.Payments.Where(p => string.Equals(p.PaymentStatus, "PENDING", StringComparison.OrdinalIgnoreCase)))
            {
                payment.PaymentStatus = "CANCELLED";
                _context.Payments.Update(payment);
            }

            await _context.SaveChangesAsync(cancellationToken);

            await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
            {
                UserId = userId,
                Message = $"Đơn hàng #{orderId} đã bị hủy."
            }, cancellationToken);

            return new BasicResponse(true, "Đơn hàng đã bị hủy.");
        }

        public async Task<BasicResponse> ConfirmAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null) return new BasicResponse(false, "Không tìm thấy đơn hàng.");
            if (order.UserId != userId) return new BasicResponse(false, "Không có quyền thực hiện.");

            if (string.Equals(order.OrderStatus, "CONFIRMED", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(true, "Đơn hàng đã được xác nhận.");
            }
            if (!string.Equals(order.OrderStatus, "PROCESSING", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(false, "Chỉ đơn hàng đang xử lý mới có thể xác nhận.");
            }

            order.OrderStatus = "CONFIRMED";
            _context.Orders.Update(order);
            await _context.SaveChangesAsync(cancellationToken);

            await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
            {
                UserId = userId,
                Message = $"Đơn hàng #{orderId} đã được xác nhận."
            }, cancellationToken);

            return new BasicResponse(true, "Đơn hàng đã được xác nhận.");
        }

        public async Task<BasicResponse> CompleteAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders
                .Include(o => o.Payments)
                .FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null) return new BasicResponse(false, "Không tìm thấy đơn hàng.");
            if (order.UserId != userId) return new BasicResponse(false, "Không có quyền thực hiện.");

            if (string.Equals(order.OrderStatus, "COMPLETED", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(true, "Đơn hàng đã hoàn tất.");
            }
            if (!string.Equals(order.OrderStatus, "CONFIRMED", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(false, "Chỉ đơn hàng đã xác nhận mới có thể hoàn tất.");
            }

            // Auto-mark payment as paid if it hasn't been
            var pendingPayment = order.Payments
                .FirstOrDefault(p => string.Equals(p.PaymentStatus, "PENDING", StringComparison.OrdinalIgnoreCase));
            if (pendingPayment != null)
            {
                pendingPayment.PaymentStatus = "PAID";
                _context.Payments.Update(pendingPayment);
            }

            order.OrderStatus = "COMPLETED";
            _context.Orders.Update(order);
            await _context.SaveChangesAsync(cancellationToken);

            await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
            {
                UserId = userId,
                Message = $"Đơn hàng #{orderId} đã hoàn tất. Cảm ơn bạn đã mua hàng!"
            }, cancellationToken);

            return new BasicResponse(true, "Đơn hàng đã hoàn tất.");
        }
    }
}
