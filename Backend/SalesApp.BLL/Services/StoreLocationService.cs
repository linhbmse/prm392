using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class StoreLocationService : IStoreLocationService
    {
        private readonly AppDbContext _context;

        public StoreLocationService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<StoreLocationDto>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            return await _context.StoreLocations
                .AsNoTracking()
                .Select(s => new StoreLocationDto
                {
                    LocationId = s.LocationId,
                    Latitude = s.Latitude,
                    Longitude = s.Longitude,
                    Address = s.Address
                })
                .ToListAsync(cancellationToken);
        }

        public async Task<StoreLocationDto?> GetByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            var entity = await _context.StoreLocations.FindAsync(new object[] { id }, cancellationToken);
            if (entity == null) return null;

            return new StoreLocationDto
            {
                LocationId = entity.LocationId,
                Latitude = entity.Latitude,
                Longitude = entity.Longitude,
                Address = entity.Address
            };
        }

        public async Task<StoreLocationDto> CreateAsync(CreateStoreLocationDto request, CancellationToken cancellationToken = default)
        {
            var entity = new StoreLocation
            {
                Latitude = request.Latitude,
                Longitude = request.Longitude,
                Address = request.Address.Trim()
            };

            await _context.StoreLocations.AddAsync(entity, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            return new StoreLocationDto
            {
                LocationId = entity.LocationId,
                Latitude = entity.Latitude,
                Longitude = entity.Longitude,
                Address = entity.Address
            };
        }

        public async Task<StoreLocationDto?> UpdateAsync(int id, UpdateStoreLocationDto request, CancellationToken cancellationToken = default)
        {
            var entity = await _context.StoreLocations.FindAsync(new object[] { id }, cancellationToken);
            if (entity == null) return null;

            if (request.Latitude.HasValue) entity.Latitude = request.Latitude.Value;
            if (request.Longitude.HasValue) entity.Longitude = request.Longitude.Value;
            if (!string.IsNullOrWhiteSpace(request.Address)) entity.Address = request.Address.Trim();

            _context.StoreLocations.Update(entity);
            await _context.SaveChangesAsync(cancellationToken);

            return new StoreLocationDto
            {
                LocationId = entity.LocationId,
                Latitude = entity.Latitude,
                Longitude = entity.Longitude,
                Address = entity.Address
            };
        }

        public async Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default)
        {
            var entity = await _context.StoreLocations.FindAsync(new object[] { id }, cancellationToken);
            if (entity == null) return false;

            _context.StoreLocations.Remove(entity);
            await _context.SaveChangesAsync(cancellationToken);
            return true;
        }
    }
}
