using System.Collections.Generic;

namespace SalesApp.BLL.DTOs
{
    public class ProductListItemDto
    {
        public int ProductId { get; set; }
        public string ProductName { get; set; }
        public string? BriefDescription { get; set; }
        public decimal Price { get; set; }
        public string? ImageUrl { get; set; }
        public int? CategoryId { get; set; }
        public string? CategoryName { get; set; }
    }

    public class ProductDetailDto : ProductListItemDto
    {
        public string? FullDescription { get; set; }
        public string? TechnicalSpecifications { get; set; }
    }

    public class ProductListResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<ProductListItemDto> Items { get; set; }
        
        public ProductListResponseDto(int total, IEnumerable<ProductListItemDto> items)
        {
            Total = total;
            Items = items;
        }
    }

    public class CreateProductDto
    {
        [System.ComponentModel.DataAnnotations.Required]
        public string ProductName { get; set; }
        public string? BriefDescription { get; set; }
        public string? FullDescription { get; set; }
        public string? TechnicalSpecifications { get; set; }
        public decimal Price { get; set; }
        public string? ImageUrl { get; set; }
        public int? CategoryId { get; set; }
    }

    public class UpdateProductDto : CreateProductDto
    {
    }
}
