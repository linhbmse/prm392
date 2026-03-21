using SalesApp.BLL.DTOs;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface ICategoryService
    {
        Task<CategoryListResponseDto> GetCategoriesAsync(string? search, int skip, int take, CancellationToken cancellationToken = default);
        Task<CategoryDto?> GetCategoryByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<CategoryDto> CreateAsync(CreateCategoryDto request, CancellationToken cancellationToken = default);
        Task<CategoryDto?> UpdateAsync(int id, UpdateCategoryDto request, CancellationToken cancellationToken = default);
        Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default);
    }
}
