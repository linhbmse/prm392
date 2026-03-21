using System;
using System.Collections.Generic;

namespace SalesApp.BLL.DTOs
{
    // ===== Admin User DTOs =====
    public class AdminUserListDto
    {
        public int UserId { get; set; }
        public string Username { get; set; }
        public string Email { get; set; }
        public string? PhoneNumber { get; set; }
        public string? Address { get; set; }
        public string Role { get; set; }
    }

    public class AdminUserListResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<AdminUserListDto> Items { get; set; }
    }

    public class AdminUpdateUserDto
    {
        public string? Email { get; set; }
        public string? PhoneNumber { get; set; }
        public string? Address { get; set; }
        public string? Role { get; set; } // "User" or "Admin"
    }

    // ===== Admin Order DTOs =====
    public class AdminOrderListDto
    {
        public int OrderId { get; set; }
        public int? UserId { get; set; }
        public string? Username { get; set; }
        public decimal TotalAmount { get; set; }
        public string? PaymentMethod { get; set; }
        public string? OrderStatus { get; set; }
        public string? BillingAddress { get; set; }
        public DateTime? OrderDate { get; set; }
    }

    public class AdminOrderListResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<AdminOrderListDto> Items { get; set; }
    }

    public class AdminUpdateOrderStatusDto
    {
        public string Status { get; set; } // PENDING, PROCESSING, CONFIRMED, COMPLETED, CANCELLED
    }

    // ===== Admin Product DTOs =====
    public class AdminProductListDto
    {
        public int ProductId { get; set; }
        public string? ProductName { get; set; }
        public decimal Price { get; set; }
        public int StockQuantity { get; set; }
        public int? CategoryId { get; set; }
        public string? CategoryName { get; set; }
        public string? ImageUrl { get; set; }
    }

    public class AdminProductListResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<AdminProductListDto> Items { get; set; }
    }
}
