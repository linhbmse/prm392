namespace SalesApp.BLL.DTOs
{
    public class PayosWebhookData
    {
        public long OrderCode { get; set; }
        public long Amount { get; set; }
        public string? Description { get; set; }
        public string? Currency { get; set; }
        public string? Status { get; set; }
        public string? PaymentLinkId { get; set; }
        public string? Reference { get; set; }
        public string? TransactionDateTime { get; set; }
    }

    public class PayosWebhookRequest
    {
        public string? Code { get; set; }
        public string? Desc { get; set; }
        public PayosWebhookData? Data { get; set; }
        public string? Signature { get; set; }
    }

    public class BasicResponse
    {
        public bool Success { get; }
        public string Message { get; }

        public BasicResponse(bool success, string message)
        {
            Success = success;
            Message = message;
        }
    }
}
