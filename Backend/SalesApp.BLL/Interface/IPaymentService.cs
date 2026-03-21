using PayOS.Models.Webhooks;
using SalesApp.BLL.DTOs;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.BLL.Interfaces
{
    public interface IPaymentService
    {
        Task<CheckoutResponseDto> CheckoutAsync(ClaimsPrincipal userPrincipal, CheckoutRequestDto request, CancellationToken cancellationToken = default);
        Task<BasicResponse> HandlePayosWebhookAsync(Webhook request, CancellationToken cancellationToken = default);
    }
}
