using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using SalesApp.DAL;
using SalesApp.Domain.Entities;
using System;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;

namespace SalesApp.BLL.Services
{
    public class AuthService : IAuthService
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _configuration;

        public AuthService(AppDbContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        public async Task<AuthResponseDto> RegisterAsync(RegisterDto model)
        {
            if (await _context.Users.AnyAsync(u => u.Username == model.Username))
            {
                return new AuthResponseDto { Success = false, Message = "Username already exists." };
            }

            if (await _context.Users.AnyAsync(u => u.Email == model.Email))
            {
                return new AuthResponseDto { Success = false, Message = "Email already exists." };
            }

            var user = new User
            {
                Username = model.Username,
                Email = model.Email,
                PhoneNumber = model.PhoneNumber,
                Role = "User", // Default role
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(model.Password)
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return new AuthResponseDto { Success = true, Message = "User registered successfully." };
        }

        public async Task<AuthResponseDto> LoginAsync(LoginDto model)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Username == model.Username);
            if (user == null)
            {
                return new AuthResponseDto { Success = false, Message = "Invalid username or password." };
            }

            bool isPasswordValid = BCrypt.Net.BCrypt.Verify(model.Password, user.PasswordHash);
            if (!isPasswordValid)
            {
                return new AuthResponseDto { Success = false, Message = "Invalid username or password." };
            }

            var token = GenerateJwtToken(user);

            return new AuthResponseDto
            {
                Success = true,
                Message = "Login successful.",
                Token = token,
                Role = user.Role
            };
        }

        public async Task<AuthResponseDto> ForgotPasswordAsync(ForgotPasswordDto model)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == model.Email);
            if (user == null)
            {
                return new AuthResponseDto { Success = true, Message = "If the email is registered, you will receive a password reset link." };
            }

            var token = GenerateJwtToken(user, isPasswordReset: true);
            
            // Console log mock to simulate Email Service
            Console.WriteLine($"\n[EMAIL MOCK] Password Reset Token for {model.Email}:\n{token}\n");

            return new AuthResponseDto { Success = true, Message = "If the email is registered, you will receive a password reset link." };
        }

        public async Task<AuthResponseDto> ResetPasswordAsync(ResetPasswordDto model)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == model.Email);
            if (user == null)
            {
                return new AuthResponseDto { Success = false, Message = "Invalid email or token." };
            }

            try
            {
                var jwtSettings = _configuration.GetSection("JwtSettings");
                var key = Encoding.ASCII.GetBytes(jwtSettings["Secret"]);
                var tokenHandler = new JwtSecurityTokenHandler();

                tokenHandler.ValidateToken(model.Token, new TokenValidationParameters
                {
                    ValidateIssuerSigningKey = true,
                    IssuerSigningKey = new SymmetricSecurityKey(key),
                    ValidateIssuer = true,
                    ValidIssuer = jwtSettings["Issuer"],
                    ValidateAudience = true,
                    ValidAudience = jwtSettings["Audience"],
                    ValidateLifetime = true,
                    ClockSkew = TimeSpan.Zero
                }, out SecurityToken validatedToken);

                var jwtToken = (JwtSecurityToken)validatedToken;
                var userIdFromToken = int.Parse(jwtToken.Claims.First(x => x.Type == ClaimTypes.NameIdentifier).Value);
                var isPasswordReset = jwtToken.Claims.FirstOrDefault(x => x.Type == "Purpose")?.Value == "PasswordReset";

                if (userIdFromToken != user.UserId || !isPasswordReset)
                {
                    return new AuthResponseDto { Success = false, Message = "Invalid token." };
                }

                user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(model.NewPassword);
                _context.Users.Update(user);
                await _context.SaveChangesAsync();

                return new AuthResponseDto { Success = true, Message = "Password has been reset successfully." };
            }
            catch (Exception)
            {
                return new AuthResponseDto { Success = false, Message = "Invalid or expired token." };
            }
        }

        private string GenerateJwtToken(User user, bool isPasswordReset = false)
        {
            var jwtSettings = _configuration.GetSection("JwtSettings");
            var key = Encoding.ASCII.GetBytes(jwtSettings["Secret"]);

            var tokenHandler = new JwtSecurityTokenHandler();
            var claims = new System.Collections.Generic.List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, user.UserId.ToString()),
                new Claim(ClaimTypes.Name, user.Username),
                new Claim(ClaimTypes.Role, user.Role)
            };

            if (isPasswordReset)
            {
                claims.Add(new Claim("Purpose", "PasswordReset"));
            }

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = isPasswordReset ? DateTime.UtcNow.AddMinutes(15) : DateTime.UtcNow.AddDays(7),
                Issuer = jwtSettings["Issuer"],
                Audience = jwtSettings["Audience"],
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }
    }
}
