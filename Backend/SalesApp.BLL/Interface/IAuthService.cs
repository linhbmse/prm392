using SalesApp.BLL.DTOs;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IAuthService
    {
        Task<AuthResponseDto> RegisterAsync(RegisterDto model);
        Task<AuthResponseDto> LoginAsync(LoginDto model);
        Task<AuthResponseDto> ForgotPasswordAsync(ForgotPasswordDto model);
        Task<AuthResponseDto> ResetPasswordAsync(ResetPasswordDto model);
    }
}
