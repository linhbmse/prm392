using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PayOS.Models.Webhooks;
using SalesApp.BLL.DTOs;
using SalesApp.BLL.Interfaces;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace SalesApp.API.Controllers
{
    [ApiController]
    [Route("api/payos/webhook")]
    [AllowAnonymous]
    public class PayOSWebhookController : ControllerBase
    {
        private readonly IPaymentService _paymentService;

        public PayOSWebhookController(IPaymentService paymentService)
        {
            _paymentService = paymentService;
        }

        [HttpPost]
        public async Task<ActionResult> ReceiveWebhook(Webhook webhook, CancellationToken cancellationToken = default)
        {
            if (webhook == null)
            {
                return BadRequest("Dữ liệu webhook là bắt buộc");
            }

            try
            {
                // Process real webhook
                var result = await _paymentService.HandlePayosWebhookAsync(webhook, cancellationToken);
                
                if (!result.Success)
                {
                    return Ok(new { error = result.Message, message = "Đã nhận webhook nhưng xử lý thất bại" });
                }

                return Ok(new { message = "Webhook processed successfully", orderCode = webhook.Data?.OrderCode });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[PayOS Webhook Error] {ex.Message}");
                return Problem(ex.Message);
            }
        }
    }
}
