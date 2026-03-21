using SalesApp.BLL.DTOs;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IStoreLocationService
    {
        Task<IEnumerable<StoreLocationDto>> GetAllAsync(CancellationToken cancellationToken = default);
        Task<StoreLocationDto?> GetByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<StoreLocationDto> CreateAsync(CreateStoreLocationDto request, CancellationToken cancellationToken = default);
        Task<StoreLocationDto?> UpdateAsync(int id, UpdateStoreLocationDto request, CancellationToken cancellationToken = default);
        Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default);
    }
}
