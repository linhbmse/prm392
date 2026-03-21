using System.ComponentModel.DataAnnotations;

namespace SalesApp.BLL.DTOs
{
    public class StoreLocationDto
    {
        public int LocationId { get; set; }
        public decimal Latitude { get; set; }
        public decimal Longitude { get; set; }
        public string Address { get; set; }
    }

    public class CreateStoreLocationDto
    {
        [Required]
        public decimal Latitude { get; set; }

        [Required]
        public decimal Longitude { get; set; }

        [Required]
        public string Address { get; set; }
    }

    public class UpdateStoreLocationDto
    {
        public decimal? Latitude { get; set; }
        public decimal? Longitude { get; set; }
        public string? Address { get; set; }
    }
}
