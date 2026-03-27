using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System;
using System.Collections.Generic;
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

            // Lấy giỏ hàng đang hoạt động của user
            var cart = await _context.Carts
                .Include(c => c.CartItems)
                    .ThenInclude(ci => ci.Product)
                .FirstOrDefaultAsync(c => c.UserId == userId && c.Status == "Active", cancellationToken);

            if (cart == null || !cart.CartItems.Any())
            {
                throw new InvalidOperationException("Giỏ hàng trống.");
            }

            // Xác định danh sách sản phẩm cần mua
            List<CartItem> itemsToBuy;
            if (request.CartItemIds != null && request.CartItemIds.Any())
            {
                // Mua lẻ: Chỉ lấy các item được user chọn dựa trên ID
                itemsToBuy = cart.CartItems.Where(ci => request.CartItemIds.Contains(ci.CartItemId)).ToList();
                if (!itemsToBuy.Any())
                {
                    throw new InvalidOperationException("Không tìm thấy các sản phẩm đã chọn trong giỏ hàng.");
                }
            }
            else
            {
                // Mua toàn bộ: Lấy tất cả item trong giỏ hàng
                itemsToBuy = cart.CartItems.ToList();
            }

            decimal totalAmount = itemsToBuy.Sum(ci => ci.Price * ci.Quantity);

            // 1. Tạo một Cart mới (Trạng thái Completed) để đại diện cho đơn hàng này
            var orderCart = new Cart
            {
                UserId = userId,
                Status = "Completed",
                TotalPrice = totalAmount
            };
            await _context.Carts.AddAsync(orderCart, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            // 2. Di chuyển các CartItem được chọn sang Cart mới (Order Cart)
            // và xóa chúng khỏi giỏ hàng hiện tại (Active Cart)
            foreach (var item in itemsToBuy)
            {
                var newCartItem = new CartItem
                {
                    CartId = orderCart.CartId,
                    ProductId = item.ProductId,
                    Quantity = item.Quantity,
                    Price = item.Price
                };
                await _context.CartItems.AddAsync(newCartItem, cancellationToken);

                // Xóa item khỏi giỏ hàng "Active" của user
                _context.CartItems.Remove(item);
            }

            // 3. Tạo đơn hàng (Order) liên kết với Cart mới
            var order = new Order
            {
                UserId = userId,
                CartId = orderCart.CartId,
                PaymentMethod = method,
                BillingAddress = request.BillingAddress.Trim(),
                OrderStatus = "PENDING",
                OrderDate = DateTime.UtcNow
            };
            await _context.Orders.AddAsync(order, cancellationToken);

            // 4. Cập nhật lại tổng tiền cho giỏ hàng Active (nếu vẫn còn sản phẩm khác)
            // Cần tính lại dựa trên database sau khi đã Remove
            await _context.SaveChangesAsync(cancellationToken); // Thực thi việc remove trước

            var remainingItemsTotal = await _context.CartItems
                .Where(ci => ci.CartId == cart.CartId)
                .SumAsync(ci => ci.Price * ci.Quantity, cancellationToken);

            cart.TotalPrice = remainingItemsTotal;
            _context.Carts.Update(cart);

            await _context.SaveChangesAsync(cancellationToken);

            // 5. Gửi thông báo cho user
            await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
            {
                UserId = userId,
                Message = $"Đơn hàng #{order.OrderId} ({method}) đã được tạo. Tổng tiền: ${totalAmount}."
            }, cancellationToken);

            return new CreateOrderResponseDto
            {
                OrderId = order.OrderId,
                TotalAmount = totalAmount,
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
                .Include(o => o.Cart)
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

            if (order == null || order.UserId != userId) return null;

            var latestPayment = order.Payments.OrderByDescending(p => p.PaymentDate).FirstOrDefault();

            var orderItems = order.Cart?.CartItems?.Select(ci => new OrderItemDto
            {
                ProductId = ci.ProductId ?? 0,
                ProductName = ci.Product != null ? ci.Product.ProductName : "Sản phẩm",
                ImageUrl = ci.Product?.ImageUrl,
                Quantity = ci.Quantity,
                Price = ci.Price
            }) ?? Enumerable.Empty<OrderItemDto>();

            return new OrderDetailDto
            {
                OrderId = order.OrderId,
                CartId = order.CartId,
                UserId = order.UserId ?? 0,
                TotalAmount = order.Cart?.TotalPrice ?? 0,
                PaymentMethod = order.PaymentMethod,
                OrderStatus = order.OrderStatus,
                BillingAddress = order.BillingAddress,
                OrderDate = order.OrderDate,
                PaymentStatus = latestPayment?.PaymentStatus,
                Items = orderItems
            };
        }

        public async Task<BasicResponse> CancelAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders
                .Include(o => o.Payments)
                .FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);

            if (order == null || order.UserId != userId) return new BasicResponse(false, "Không tìm thấy.");

            order.OrderStatus = "CANCELLED";
            foreach (var p in order.Payments.Where(p => p.PaymentStatus == "PENDING")) p.PaymentStatus = "CANCELLED";

            await _context.SaveChangesAsync(cancellationToken);
            return new BasicResponse(true, "Đã hủy đơn.");
        }

        public async Task<BasicResponse> ConfirmAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders.FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);
            if (order == null || order.UserId != userId) return new BasicResponse(false, "Không tìm thấy.");

            order.OrderStatus = "CONFIRMED";
            await _context.SaveChangesAsync(cancellationToken);
            return new BasicResponse(true, "Đã xác nhận.");
        }

        public async Task<BasicResponse> CompleteAsync(ClaimsPrincipal userPrincipal, int orderId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var order = await _context.Orders.Include(o => o.Payments).FirstOrDefaultAsync(o => o.OrderId == orderId, cancellationToken);
            if (order == null || order.UserId != userId) return new BasicResponse(false, "Không tìm thấy.");

            var p = order.Payments.FirstOrDefault(p => p.PaymentStatus == "PENDING");
            if (p != null) p.PaymentStatus = "PAID";

            order.OrderStatus = "COMPLETED";
            await _context.SaveChangesAsync(cancellationToken);
            return new BasicResponse(true, "Đã hoàn tất.");
        }
    }
}
