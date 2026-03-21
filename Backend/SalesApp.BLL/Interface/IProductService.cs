using SalesApp.BLL.DTOs;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IProductService
    {
        Task<ProductListResponseDto> GetProductsAsync(
            string? search, 
            int? categoryId, 
            decimal? minPrice, 
            decimal? maxPrice, 
            string? sort, 
            int skip, 
            int take, 
            CancellationToken cancellationToken = default);

        Task<ProductDetailDto?> GetProductByIdAsync(int productId, CancellationToken cancellationToken = default);
        Task<ProductDetailDto> CreateAsync(CreateProductDto request, CancellationToken cancellationToken = default);
        Task<ProductDetailDto?> UpdateAsync(int id, UpdateProductDto request, CancellationToken cancellationToken = default);
        Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default);
    }
}
