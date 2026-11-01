import logging
import os

import httpx

logger = logging.getLogger(__name__)


class PushSender:
    """Sends push notifications via FCM/APNs (stubbed HTTP client)."""

    def __init__(self) -> None:
        self.endpoint = os.getenv("PUSH_GATEWAY_URL", "http://push-gateway:8080/send")
        self.service_token = os.getenv("PUSH_SERVICE_TOKEN", "")

    async def send(self, device_token: str, title: str, body: str, payload: dict | None = None) -> bool:
        request = {
            "token": device_token,
            "notification": {"title": title, "body": body},
            "data": payload or {},
        }
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                resp = await client.post(
                    self.endpoint,
                    json=request,
                    headers={"Authorization": f"Bearer {self.service_token}"},
                )
                resp.raise_for_status()
                return True
        except Exception:
            logger.exception("Push send failed for token=%s", device_token[:8])
            return False
