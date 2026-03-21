using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.DAL;
using SalesApp.BLL.Interfaces;
using SalesApp.Domain.Entities;
using System;
using System.Linq;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class CartService : ICartService
    {
        private readonly AppDbContext _context;

        public CartService(AppDbContext context)
        {
            _context = context;
        }

        private int GetUserId(ClaimsPrincipal principal)
        {
            var idClaim = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(idClaim) || !int.TryParse(idClaim, out int userId))
            {
                throw new UnauthorizedAccessException("Người dùng không hợp lệ.");
            }
            return userId;
        }

        private async Task<Cart> EnsureCartAsync(int userId, CancellationToken cancellationToken)
        {
            var cart = await _context.Carts
                .FirstOrDefaultAsync(c => c.UserId == userId && c.Status == "Active", cancellationToken);
            
            if (cart != null) return cart;

            cart = new Cart
            {
                UserId = userId,
                Status = "Active",
                TotalPrice = 0
            };
            
            await _context.Carts.AddAsync(cart, cancellationToken);
            await _context.SaveChangesAsync(cancellationToken);
            
            return cart;
        }

        private async Task UpdateCartTotal(Cart cart, CancellationToken cancellationToken)
        {
            var items = await _context.CartItems
                .Where(i => i.CartId == cart.CartId)
                .ToListAsync(cancellationToken);
            
            cart.TotalPrice = items.Sum(i => i.Price * i.Quantity);
            _context.Carts.Update(cart);
            await _context.SaveChangesAsync(cancellationToken);
        }

        public async Task<CartResponseDto> GetCartAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var cart = await EnsureCartAsync(userId, cancellationToken);

            var items = await _context.CartItems
                .Include(i => i.Product)
                .Where(i => i.CartId == cart.CartId)
                .Select(i => new CartItemResponseDto
                {
                    CartItemId = i.CartItemId,
                    ProductId = i.ProductId ?? 0,
                    ProductName = i.Product != null ? i.Product.ProductName : "Sản phẩm không tồn tại",
                    ImageUrl = i.Product != null ? i.Product.ImageUrl : null,
                    Quantity = i.Quantity,
                    Price = i.Price
                })
                .ToListAsync(cancellationToken);

            return new CartResponseDto
            {
                CartId = cart.CartId,
                Status = cart.Status,
                TotalPrice = cart.TotalPrice,
                Items = items
            };
        }

        public async Task<CartItemResponseDto> AddItemAsync(ClaimsPrincipal userPrincipal, AddCartItemRequestDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var cart = await EnsureCartAsync(userId, cancellationToken);

            var product = await _context.Products.FirstOrDefaultAsync(p => p.ProductId == request.ProductId, cancellationToken);
            if (product == null)
            {
                throw new InvalidOperationException("Không tìm thấy sản phẩm.");
            }

            var existingItem = await _context.CartItems
                .FirstOrDefaultAsync(i => i.CartId == cart.CartId && i.ProductId == request.ProductId, cancellationToken);

            if (existingItem != null)
            {
                existingItem.Quantity += request.Quantity;
                _context.CartItems.Update(existingItem);
            }
            else
            {
                existingItem = new CartItem
                {
                    CartId = cart.CartId,
                    ProductId = request.ProductId,
                    Quantity = request.Quantity,
                    Price = product.Price
                };
                await _context.CartItems.AddAsync(existingItem, cancellationToken);
            }

            await _context.SaveChangesAsync(cancellationToken);
            await UpdateCartTotal(cart, cancellationToken);

            return new CartItemResponseDto
            {
                CartItemId = existingItem.CartItemId,
                ProductId = existingItem.ProductId ?? 0,
                ProductName = product.ProductName,
                ImageUrl = product.ImageUrl,
                Quantity = existingItem.Quantity,
                Price = existingItem.Price
            };
        }

        public async Task<bool> UpdateItemAsync(ClaimsPrincipal userPrincipal, int cartItemId, UpdateCartItemRequestDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var item = await _context.CartItems
                .Include(i => i.Cart)
                .FirstOrDefaultAsync(i => i.CartItemId == cartItemId, cancellationToken);

            if (item == null || item.Cart == null || item.Cart.UserId != userId)
            {
                return false;
            }

            item.Quantity = request.Quantity;
            _context.CartItems.Update(item);
            await _context.SaveChangesAsync(cancellationToken);
            await UpdateCartTotal(item.Cart, cancellationToken);

            return true;
        }

        public async Task<bool> RemoveItemAsync(ClaimsPrincipal userPrincipal, int cartItemId, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var item = await _context.CartItems
                .Include(i => i.Cart)
                .FirstOrDefaultAsync(i => i.CartItemId == cartItemId, cancellationToken);

            if (item == null || item.Cart == null || item.Cart.UserId != userId)
            {
                return false;
            }

            _context.CartItems.Remove(item);
            await _context.SaveChangesAsync(cancellationToken);
            await UpdateCartTotal(item.Cart, cancellationToken);

            return true;
        }

        public async Task<bool> ClearAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var cart = await _context.Carts
                .FirstOrDefaultAsync(c => c.UserId == userId && c.Status == "Active", cancellationToken);

            if (cart == null) return true;

            var items = await _context.CartItems.Where(i => i.CartId == cart.CartId).ToListAsync(cancellationToken);
            if (items.Any())
            {
                _context.CartItems.RemoveRange(items);
                cart.TotalPrice = 0;
                _context.Carts.Update(cart);
                await _context.SaveChangesAsync(cancellationToken);
            }

            return true;
        }
    }
}
