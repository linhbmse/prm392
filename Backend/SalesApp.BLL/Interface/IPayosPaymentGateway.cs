using SalesApp.BLL.DTOs;

namespace SalesApp.BLL.Interfaces
{
    public interface IPayosPaymentGateway
    {
        bool VerifyWebhookSignature(PayosWebhookRequest request, out string message);
    }
}
