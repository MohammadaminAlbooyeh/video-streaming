import logging
import os

import httpx

logger = logging.getLogger(__name__)


class EmailSender:
    """Sends transactional email via an SMTP/HTTP provider."""

    def __init__(self) -> None:
        self.endpoint = os.getenv("EMAIL_PROVIDER_URL", "http://email-provider:8080/send")
        self.from_address = os.getenv("EMAIL_FROM", "no-reply@vsp.example")

    async def send(self, to: str, subject: str, body: str) -> bool:
        request = {
            "from": self.from_address,
            "to": to,
            "subject": subject,
            "html": body,
        }
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                resp = await client.post(self.endpoint, json=request)
                resp.raise_for_status()
                return True
        except Exception:
            logger.exception("Email send failed for to=%s", to)
            return False
