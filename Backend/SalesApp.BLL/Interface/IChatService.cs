using SalesApp.BLL.DTOs;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IChatService
    {
        Task<ChatHistoryResponseDto> GetMessagesAsync(ClaimsPrincipal userPrincipal, int? otherUserId, int skip = 0, int take = 50, CancellationToken cancellationToken = default);
        Task<ChatMessageDto> SendMessageAsync(ClaimsPrincipal userPrincipal, SendMessageDto request, CancellationToken cancellationToken = default);
    }
}
