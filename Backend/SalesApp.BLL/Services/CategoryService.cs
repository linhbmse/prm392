using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class CategoryService : ICategoryService
    {
        private readonly AppDbContext _context;

        public CategoryService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<CategoryListResponseDto> GetCategoriesAsync(string? search, int skip, int take, CancellationToken cancellationToken = default)
        {
            var query = _context.Categories.AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                query = query.Where(c => c.CategoryName.Contains(search));
            }

            var total = await query.CountAsync(cancellationToken);

            var items = await query
                .OrderBy(c => c.CategoryName)
                .Skip(skip < 0 ? 0 : skip)
                .Take(take <= 0 ? 20 : Math.Min(take, 100))
                .Select(c => new CategoryDto
                {
                    CategoryId = c.CategoryId,
                    CategoryName = c.CategoryName
                })
                .ToListAsync(cancellationToken);

            return new CategoryListResponseDto(total, items);
        }

        public async Task<CategoryDto?> GetCategoryByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            var category = await _context.Categories.FirstOrDefaultAsync(c => c.CategoryId == id, cancellationToken);
            if (category == null) return null;

            return new CategoryDto
            {
                CategoryId = category.CategoryId,
                CategoryName = category.CategoryName
            };
        }

        public async Task<CategoryDto> CreateAsync(CreateCategoryDto request, CancellationToken cancellationToken = default)
        {
            var entity = new Category
            {
                CategoryName = request.CategoryName
            };

            await _context.Categories.AddAsync(entity, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);

            return new CategoryDto
            {
                CategoryId = entity.CategoryId,
                CategoryName = entity.CategoryName
            };
        }

        public async Task<CategoryDto?> UpdateAsync(int id, UpdateCategoryDto request, CancellationToken cancellationToken = default)
        {
            var entity = await _context.Categories.FirstOrDefaultAsync(c => c.CategoryId == id, cancellationToken);
            if (entity == null) return null;

            entity.CategoryName = request.CategoryName;

            _context.Categories.Update(entity);
            await _context.SaveChangesAsync(cancellationToken);

            return new CategoryDto
            {
                CategoryId = entity.CategoryId,
                CategoryName = entity.CategoryName
            };
        }

        public async Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default)
        {
            var entity = await _context.Categories.FirstOrDefaultAsync(c => c.CategoryId == id, cancellationToken);
            if (entity == null) return false;

            _context.Categories.Remove(entity);
            await _context.SaveChangesAsync(cancellationToken);

            return true;
        }
    }
}
