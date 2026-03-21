using Microsoft.EntityFrameworkCore;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using System;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class UserService : IUserService
    {
        private readonly AppDbContext _context;

        public UserService(AppDbContext context)
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

        public async Task<UserProfileDto> GetProfileAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var user = await _context.Users.FirstOrDefaultAsync(u => u.UserId == userId, cancellationToken);
            if (user == null) throw new UnauthorizedAccessException("Người dùng không tồn tại.");

            return new UserProfileDto
            {
                UserId = user.UserId,
                Username = user.Username,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                Address = user.Address,
                Role = user.Role
            };
        }

        public async Task<UserProfileDto> UpdateProfileAsync(ClaimsPrincipal userPrincipal, UpdateProfileDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var user = await _context.Users.FirstOrDefaultAsync(u => u.UserId == userId, cancellationToken);
            if (user == null) throw new UnauthorizedAccessException("Người dùng không tồn tại.");

            if (!string.IsNullOrWhiteSpace(request.Email))
            {
                var emailExists = await _context.Users.AnyAsync(u => u.Email == request.Email.Trim() && u.UserId != userId, cancellationToken);
                if (emailExists) throw new InvalidOperationException("Email đã được sử dụng bởi tài khoản khác.");
                user.Email = request.Email.Trim();
            }
            if (request.PhoneNumber != null) user.PhoneNumber = request.PhoneNumber.Trim();
            if (request.Address != null) user.Address = request.Address.Trim();

            _context.Users.Update(user);
            await _context.SaveChangesAsync(cancellationToken);

            return new UserProfileDto
            {
                UserId = user.UserId,
                Username = user.Username,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                Address = user.Address,
                Role = user.Role
            };
        }

        public async Task<BasicResponse> ChangePasswordAsync(ClaimsPrincipal userPrincipal, ChangePasswordDto request, CancellationToken cancellationToken = default)
        {
            var userId = GetUserId(userPrincipal);
            var user = await _context.Users.FirstOrDefaultAsync(u => u.UserId == userId, cancellationToken);
            if (user == null) return new BasicResponse(false, "Người dùng không tồn tại.");

            if (!BCrypt.Net.BCrypt.Verify(request.CurrentPassword, user.PasswordHash))
            {
                return new BasicResponse(false, "Mật khẩu hiện tại không đúng.");
            }

            if (string.IsNullOrWhiteSpace(request.NewPassword) || request.NewPassword.Length < 6)
            {
                return new BasicResponse(false, "Mật khẩu mới phải có ít nhất 6 ký tự.");
            }

            user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.NewPassword);
            _context.Users.Update(user);
            await _context.SaveChangesAsync(cancellationToken);

            return new BasicResponse(true, "Đổi mật khẩu thành công.");
        }
    }
}
