using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class CategoryDto
    {
        public int CategoryId { get; set; }
        public string CategoryName { get; set; }
    }

    public class CategoryListResponseDto
    {
        public int Total { get; set; }
        public IEnumerable<CategoryDto> Items { get; set; }
        
        public CategoryListResponseDto(int total, IEnumerable<CategoryDto> items)
        {
            Total = total;
            Items = items;
        }
    }

    public class CreateCategoryDto
    {
        [Required]
        public string CategoryName { get; set; }
    }

    public class UpdateCategoryDto
    {
        [Required]
        public string CategoryName { get; set; }
    }
}
