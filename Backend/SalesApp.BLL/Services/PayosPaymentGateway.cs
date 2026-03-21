using Microsoft.Extensions.Configuration;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

namespace SalesApp.BLL.Services
{
    public class PayosPaymentGateway : IPayosPaymentGateway
    {
        private readonly IConfiguration _configuration;

        public PayosPaymentGateway(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        public bool VerifyWebhookSignature(PayosWebhookRequest request, out string message)
        {
            var checksumKey = _configuration["PayOS:ChecksumKey"];
            if (string.IsNullOrWhiteSpace(checksumKey))
            {
                message = "Chưa cấu hình PayOS.";
                return false;
            }
            if (request == null || request.Data == null)
            {
                message = "Thiếu dữ liệu webhook.";
                return false;
            }
            if (string.IsNullOrWhiteSpace(request.Signature))
            {
                message = "Signature là bắt buộc.";
                return false;
            }

            var data = BuildWebhookSignatureData(request.Data);
            var computed = ComputeSignature(data, checksumKey);
            var provided = request.Signature.Trim();

            if (!string.Equals(computed, provided, StringComparison.OrdinalIgnoreCase))
            {
                message = "Chữ ký PayOS không hợp lệ.";
                return false;
            }

            message = "Signature verified.";
            return true;
        }

        private static Dictionary<string, object?> BuildWebhookSignatureData(PayosWebhookData data)
        {
            var payload = new Dictionary<string, object?>
            {
                ["orderCode"] = data.OrderCode,
                ["amount"] = data.Amount
            };

            if (!string.IsNullOrWhiteSpace(data.Description)) payload["description"] = data.Description;
            if (!string.IsNullOrWhiteSpace(data.Currency)) payload["currency"] = data.Currency;
            if (!string.IsNullOrWhiteSpace(data.Status)) payload["status"] = data.Status;
            if (!string.IsNullOrWhiteSpace(data.PaymentLinkId)) payload["paymentLinkId"] = data.PaymentLinkId;
            if (!string.IsNullOrWhiteSpace(data.Reference)) payload["reference"] = data.Reference;
            if (!string.IsNullOrWhiteSpace(data.TransactionDateTime)) payload["transactionDateTime"] = data.TransactionDateTime;

            return payload;
        }

        private static string ComputeSignature(Dictionary<string, object?> payload, string checksumKey)
        {
            var parts = payload
                .Where(kvp => kvp.Value != null)
                .OrderBy(kvp => kvp.Key, StringComparer.Ordinal)
                .Select(kvp => $"{kvp.Key}={SerializeValue(kvp.Value)}");
            var raw = string.Join("&", parts);

            var keyBytes = Encoding.UTF8.GetBytes(checksumKey);
            using var hmac = new HMACSHA256(keyBytes);
            var hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(raw));
            return Convert.ToHexString(hash).ToLowerInvariant();
        }

        private static string SerializeValue(object? value)
        {
            if (value == null) return string.Empty;
            return Convert.ToString(value, System.Globalization.CultureInfo.InvariantCulture) ?? string.Empty;
        }
    }
}
