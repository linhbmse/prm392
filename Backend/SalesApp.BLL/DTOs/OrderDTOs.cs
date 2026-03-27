using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class OrderListItemDto
    {
        public int OrderId { get; set; }
        public int UserId { get; set; }
        public decimal? TotalAmount { get; set; }
        public string PaymentMethod { get; set; }
        public string OrderStatus { get; set; }
        public string BillingAddress { get; set; }
        public DateTime? OrderDate { get; set; }
    }

    public class OrderListResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<OrderListItemDto> Items { get; set; }
    }

    public class OrderDetailDto
    {
        public int OrderId { get; set; }
        public int? CartId { get; set; }
        public int UserId { get; set; }
        public decimal? TotalAmount { get; set; }
        public string PaymentMethod { get; set; }
        public string OrderStatus { get; set; }
        public string BillingAddress { get; set; }
        public DateTime? OrderDate { get; set; }
        public string? PaymentStatus { get; set; }
        public IEnumerable<OrderItemDto> Items { get; set; }
    }

    public class OrderItemDto
    {
        public int ProductId { get; set; }
        public string ProductName { get; set; }
        public string? ImageUrl { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
    }

    public class CreateOrderDto
    {
        [Required]
        public string BillingAddress { get; set; }

        [Required]
        public string PaymentMethod { get; set; } // "PAYOS" or "COD"

        public List<int>? CartItemIds { get; set; }
    }

    public class CreateOrderResponseDto
    {
        public int OrderId { get; set; }
        public decimal TotalAmount { get; set; }
        public string PaymentMethod { get; set; }
        public string OrderStatus { get; set; }
        public string BillingAddress { get; set; }
        public DateTime? OrderDate { get; set; }
    }
}
