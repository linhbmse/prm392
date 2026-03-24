using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using PayOS;
using PayOS.Models.V2.PaymentRequests;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class PaymentService : IPaymentService
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _configuration;
        private readonly IPayosPaymentGateway _payosGateway;
        private readonly INotificationService _notificationService;

        public PaymentService(AppDbContext context, IConfiguration configuration, IPayosPaymentGateway payosGateway, INotificationService notificationService)
        {
            _context = context;
            _configuration = configuration;
            _payosGateway = payosGateway;
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

        public async Task<CheckoutResponseDto> CheckoutAsync(ClaimsPrincipal userPrincipal, CheckoutRequestDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);

            // Find the order
            var order = await _context.Orders
                .Include(o => o.Cart)
                .FirstOrDefaultAsync(o => o.OrderId == request.OrderId, cancellationToken);

            if (order == null)
            {
                return new CheckoutResponseDto { Success = false, Message = "Không tìm thấy đơn hàng." };
            }
            if (order.UserId != userId)
            {
                return new CheckoutResponseDto { Success = false, Message = "Không có quyền thực hiện." };
            }
            if (!string.Equals(order.OrderStatus, "PENDING", StringComparison.OrdinalIgnoreCase))
            {
                return new CheckoutResponseDto { Success = false, Message = "Đơn hàng không ở trạng thái chờ thanh toán." };
            }

            var totalAmount = order.Cart?.TotalPrice ?? 0;
            if (totalAmount <= 0)
            {
                return new CheckoutResponseDto { Success = false, Message = "Tổng tiền đơn hàng không hợp lệ." };
            }

            // COD: không cần tạo payment link, chuyển thẳng sang PROCESSING
            if (string.Equals(order.PaymentMethod, "COD", StringComparison.OrdinalIgnoreCase))
            {
                order.OrderStatus = "PROCESSING";
                _context.Orders.Update(order);

                var codPayment = new Payment
                {
                    OrderId = order.OrderId,
                    Amount = totalAmount,
                    PaymentStatus = "COD_PENDING",
                    PaymentDate = DateTime.UtcNow
                };
                await _context.Payments.AddAsync(codPayment, cancellationToken);
                await _context.SaveChangesAsync(cancellationToken);

                await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
                {
                    UserId = userId,
                    Message = $"Đơn hàng #{order.OrderId} đã được xác nhận (COD). Thanh toán khi nhận hàng."
                }, cancellationToken);

                return new CheckoutResponseDto
                {
                    Success = true,
                    Message = "Đặt hàng COD thành công. Thanh toán khi nhận hàng.",
                    OrderId = order.OrderId,
                    CheckoutUrl = null
                };
            }

            // Create payment record
            var payment = new Payment
            {
                OrderId = order.OrderId,
                Amount = totalAmount,
                PaymentStatus = "PENDING",
                PaymentDate = DateTime.UtcNow
            };

            await _context.Payments.AddAsync(payment, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            // Configure PayOS Client
            var clientId = _configuration["PayOS:ClientId"];
            var apiKey = _configuration["PayOS:ApiKey"];
            var checksumKey = _configuration["PayOS:ChecksumKey"];
            var frontendUrl = _configuration["FrontendBaseUrl"] ?? "myapp:/";

            var payOsClient = new PayOSClient(clientId, apiKey, checksumKey);

            var amountVal = Convert.ToInt64(decimal.Round(totalAmount, 0));
            var desc = $"Order {order.OrderId}";
            if (desc.Length > 25) desc = desc.Substring(0, 25);

            var paymentRequest = new CreatePaymentLinkRequest
            {
                OrderCode = payment.PaymentId,
                Amount = amountVal,
                Description = desc,
                ReturnUrl = $"{frontendUrl}/payos-return",
                CancelUrl = $"{frontendUrl}/payos-return",
                Items = new List<PaymentLinkItem>
                {
                    new PaymentLinkItem
                    {
                        Name = desc,
                        Quantity = 1,
                        Price = amountVal
                    }
                }
            };

            try
            {
                var payOsResponse = await payOsClient.PaymentRequests.CreateAsync(paymentRequest);

                return new CheckoutResponseDto
                {
                    Success = true,
                    Message = "Tạo link thanh toán thành công",
                    CheckoutUrl = payOsResponse.CheckoutUrl,
                    OrderId = order.OrderId
                };
            }
            catch (Exception ex)
            {
                return new CheckoutResponseDto { Success = false, Message = $"Lỗi tạo Payment Link PayOS: {ex.Message}" };
            }
        }

        public async Task<BasicResponse> HandlePayosWebhookAsync(PayOS.Models.Webhooks.Webhook request, CancellationToken cancellationToken = default)
        {
            if (request == null || request.Data == null)
            {
                return new BasicResponse(false, "Dữ liệu webhook PayOS là bắt buộc.");
            }

            PayOS.Models.Webhooks.WebhookData webhookData;
            try
            {
                var clientId = _configuration["PayOS:ClientId"];
                var apiKey = _configuration["PayOS:ApiKey"];
                var checksumKey = _configuration["PayOS:ChecksumKey"];
                var payOsClient = new PayOSClient(clientId, apiKey, checksumKey);

                // Verify the webhook using the official SDK calculation
                webhookData = await payOsClient.Webhooks.VerifyAsync(request);
            }
            catch (Exception ex)
            {
                return new BasicResponse(false, $"Chữ ký PayOS không hợp lệ: {ex.Message}");
            }

            var orderCode = webhookData.OrderCode;
            var payment = await _context.Payments
                .Include(p => p.Order)
                .FirstOrDefaultAsync(p => p.PaymentId == orderCode, cancellationToken);

            if (payment == null) return new BasicResponse(false, "Không tìm thấy thanh toán.");

            if (payment.Order != null && !string.Equals(payment.Order.PaymentMethod, "PAYOS", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(false, "Phương thức thanh toán không phải PayOS.");
            }

            if (payment.Amount != webhookData.Amount)
            {
                return new BasicResponse(false, "Số tiền không khớp.");
            }

            var nextStatus = request.Code == "00" ? "PAID" : "FAILED";

            if (string.Equals(payment.PaymentStatus, nextStatus, StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(true, "Thanh toán đã ở trạng thái yêu cầu.");
            }

            if (string.Equals(payment.PaymentStatus, "PAID", StringComparison.OrdinalIgnoreCase))
            {
                return new BasicResponse(false, $"Chuyển trạng thái thanh toán không hợp lệ: {payment.PaymentStatus} -> {nextStatus}.");
            }

            payment.PaymentStatus = nextStatus;
            _context.Payments.Update(payment);
            await _context.SaveChangesAsync(cancellationToken);

            if (payment.Order != null)
            {
                payment.Order.OrderStatus = nextStatus == "PAID" ? "PROCESSING" : "PAYMENT_FAILED";
                _context.Orders.Update(payment.Order);
                await _context.SaveChangesAsync(cancellationToken);

                // Gửi notification cho user
                if (payment.Order.UserId.HasValue)
                {
                    var message = nextStatus == "PAID"
                        ? $"Thanh toán đơn hàng #{payment.Order.OrderId} thành công. Đơn hàng đang được xử lý."
                        : $"Thanh toán đơn hàng #{payment.Order.OrderId} thất bại. Vui lòng thử lại.";

                    await _notificationService.CreateAsync(new DTOs.CreateNotificationDto
                    {
                        UserId = payment.Order.UserId.Value,
                        Message = message
                    }, cancellationToken);
                }
            }

            return new BasicResponse(true, "Đã cập nhật thanh toán.");
        }
    }
}
