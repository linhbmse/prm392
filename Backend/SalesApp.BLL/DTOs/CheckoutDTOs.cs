using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class CheckoutRequestDto
    {
        [Required]
        public int OrderId { get; set; }
        
        public string? ReturnUrl { get; set; }
    }

    public class CheckoutResponseDto
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public string? CheckoutUrl { get; set; }
        public int? OrderId { get; set; }
    }
}
