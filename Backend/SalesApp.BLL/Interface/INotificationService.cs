using SalesApp.BLL.DTOs;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface INotificationService
    {
        Task<NotificationListResponseDto> GetMyNotificationsAsync(ClaimsPrincipal userPrincipal, int skip = 0, int take = 20, CancellationToken cancellationToken = default);
        Task<BasicResponse> MarkReadAsync(ClaimsPrincipal userPrincipal, int notificationId, CancellationToken cancellationToken = default);
        Task<BasicResponse> MarkAllReadAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default);
        Task<BasicResponse> CreateAsync(CreateNotificationDto request, CancellationToken cancellationToken = default);
        Task<NotificationBadgeDto> GetBadgeAsync(ClaimsPrincipal userPrincipal, CancellationToken cancellationToken = default);
    }
}
