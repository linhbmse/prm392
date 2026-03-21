using SalesApp.BLL.DTOs;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IUserService
    {
        Task<UserProfileDto> GetProfileAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default);
        Task<UserProfileDto> UpdateProfileAsync(ClaimsPrincipal userPrincipal, UpdateProfileDto request, CancellationToken cancellationToken = default);
        Task<BasicResponse> ChangePasswordAsync(ClaimsPrincipal userPrincipal, ChangePasswordDto request, CancellationToken cancellationToken = default);
    }
}
