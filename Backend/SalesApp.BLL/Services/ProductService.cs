using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class ProductService : IProductService
    {
        private readonly AppDbContext _context;

        public ProductService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<ProductListResponseDto> GetProductsAsync(
            string? search, 
            int? categoryId, 
            decimal? minPrice, 
            decimal? maxPrice, 
            string? sort, 
            int skip, 
            int take, 
            CancellationToken cancellationToken = default)
        {
            var query = _context.Products
                .Include(p => p.Category)
                .AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                query = query.Where(p => p.ProductName.Contains(search) || 
                                        (p.BriefDescription != null && p.BriefDescription.Contains(search)));
            }
            if (categoryId.HasValue)
            {
                query = query.Where(p => p.CategoryId == categoryId.Value);
            }
            if (minPrice.HasValue)
            {
                query = query.Where(p => p.Price >= minPrice.Value);
            }
            if (maxPrice.HasValue)
            {
                query = query.Where(p => p.Price <= maxPrice.Value);
            }

            var total = await query.CountAsync(cancellationToken);

            var ordered = sort?.ToLowerInvariant() switch
            {
                "price_asc" => query.OrderBy(p => p.Price),
                "price_desc" => query.OrderByDescending(p => p.Price),
                "name" => query.OrderBy(p => p.ProductName),
                _ => query.OrderByDescending(p => p.ProductId) // Default newest
            };

            var items = await ordered
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(p => new ProductListItemDto
                {
                    ProductId = p.ProductId,
                    ProductName = p.ProductName,
                    BriefDescription = p.BriefDescription,
                    Price = p.Price,
                    ImageUrl = p.ImageUrl,
                    CategoryId = p.CategoryId,
                    CategoryName = p.Category != null ? p.Category.CategoryName : null
                })
                .ToListAsync(cancellationToken);

            return new ProductListResponseDto(total, items);
        }

        public async Task<ProductDetailDto?> GetProductByIdAsync(int productId, CancellationToken cancellationToken = default)
        {
            var product = await _context.Products
                .Include(p => p.Category)
                .FirstOrDefaultAsync(p => p.ProductId == productId, cancellationToken);
            
            if (product == null) return null;

            return new ProductDetailDto
            {
                ProductId = product.ProductId,
                ProductName = product.ProductName,
                BriefDescription = product.BriefDescription,
                FullDescription = product.FullDescription,
                TechnicalSpecifications = product.TechnicalSpecifications,
                Price = product.Price,
                ImageUrl = product.ImageUrl,
                CategoryId = product.CategoryId,
                CategoryName = product.Category?.CategoryName
            };
        }

        public async Task<ProductDetailDto> CreateAsync(CreateProductDto request, CancellationToken cancellationToken = default)
        {
            var entity = new SalesApp.Domain.Entities.Product
            {
                ProductName = request.ProductName,
                BriefDescription = request.BriefDescription,
                FullDescription = request.FullDescription,
                TechnicalSpecifications = request.TechnicalSpecifications,
                Price = request.Price,
                ImageUrl = request.ImageUrl,
                CategoryId = request.CategoryId
            };

            await _context.Products.AddAsync(entity, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            return await GetProductByIdAsync(entity.ProductId, cancellationToken);
        }

        public async Task<ProductDetailDto?> UpdateAsync(int id, UpdateProductDto request, CancellationToken cancellationToken = default)
        {
            var entity = await _context.Products.FirstOrDefaultAsync(p => p.ProductId == id, cancellationToken);
            if (entity == null) return null;

            entity.ProductName = request.ProductName;
            entity.BriefDescription = request.BriefDescription;
            entity.FullDescription = request.FullDescription;
            entity.TechnicalSpecifications = request.TechnicalSpecifications;
            entity.Price = request.Price;
            entity.ImageUrl = request.ImageUrl;
            entity.CategoryId = request.CategoryId;

            _context.Products.Update(entity);
            await _context.SaveChangesAsync(cancellationToken);

            return await GetProductByIdAsync(entity.ProductId, cancellationToken);
        }

        public async Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default)
        {
            var entity = await _context.Products.FirstOrDefaultAsync(p => p.ProductId == id, cancellationToken);
            if (entity == null) return false;

            _context.Products.Remove(entity);
            await _context.SaveChangesAsync(cancellationToken);

            return true;
        }
    }
}
