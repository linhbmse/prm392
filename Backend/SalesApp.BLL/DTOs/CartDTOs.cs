using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class CartItemResponseDto
    {
        public int CartItemId { get; set; }
        public int ProductId { get; set; }
        public string ProductName { get; set; }
        public string? ImageUrl { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
        public decimal TotalPrice => Quantity * Price;
    }

    public class CartResponseDto
    {
        public int CartId { get; set; }
        public decimal TotalPrice { get; set; }
        public string Status { get; set; }
        public IEnumerable<CartItemResponseDto> Items { get; set; }
    }

    public class AddCartItemRequestDto
    {
        [Required]
        public int ProductId { get; set; }
        
        [Range(1, 1000)]
        public int Quantity { get; set; } = 1;
    }

    public class UpdateCartItemRequestDto
    {
        [Range(1, 1000)]
        public int Quantity { get; set; }
    }
}
