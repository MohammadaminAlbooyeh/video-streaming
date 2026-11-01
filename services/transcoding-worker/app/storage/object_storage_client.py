import logging
import os
from typing import Any

import boto3
from botocore.client import Config

logger = logging.getLogger(__name__)


class ObjectStorageClient:
    """S3-compatible object storage client (MinIO, AWS S3, GCS via interop)."""

    def __init__(self) -> None:
        endpoint = os.getenv("OBJECT_STORAGE_ENDPOINT", "http://minio:9000")
        self.bucket = os.getenv("OBJECT_STORAGE_BUCKET", "vsp-media")
        self.client = boto3.client(
            "s3",
            endpoint_url=endpoint,
            aws_access_key_id=os.getenv("OBJECT_STORAGE_ACCESS_KEY", "minioadmin"),
            aws_secret_access_key=os.getenv("OBJECT_STORAGE_SECRET_KEY", "minioadmin"),
            config=Config(signature_version="s3v4"),
            region_name="us-east-1",
        )
        self._ensure_bucket()

    def _ensure_bucket(self) -> None:
        try:
            self.client.head_bucket(Bucket=self.bucket)
        except Exception:
            self.client.create_bucket(Bucket=self.bucket)

    def upload(self, local_path: str, object_key: str) -> None:
        self.client.upload_file(local_path, self.bucket, object_key)
        logger.info("Uploaded %s -> s3://%s/%s", local_path, self.bucket, object_key)

    def download(self, object_key: str, local_dir: str) -> str:
        os.makedirs(local_dir, exist_ok=True)
        local_path = os.path.join(local_dir, os.path.basename(object_key))
        self.client.download_file(self.bucket, object_key, local_path)
        logger.info("Downloaded s3://%s/%s -> %s", self.bucket, object_key, local_path)
        return local_path

    def upload_tree(self, local_root: str, object_prefix: str) -> None:
        for dirpath, _, filenames in os.walk(local_root):
            for filename in filenames:
                full_path = os.path.join(dirpath, filename)
                relative = os.path.relpath(full_path, local_root)
                self.upload(full_path, f"{object_prefix}/{relative}")

    def delete_prefix(self, object_prefix: str) -> None:
        paginator = self.client.get_paginator("list_objects_v2")
        for page in paginator.paginate(Bucket=self.bucket, Prefix=object_prefix):
            contents = page.get("Contents", [])
            if contents:
                self.client.delete_objects(
                    Bucket=self.bucket,
                    Delete={"Objects": [{"Key": obj["Key"]} for obj in contents]},
                )

    def presigned_get(self, object_key: str, expires_in: int = 3600) -> str:
        return self.client.generate_presigned_url(
            "get_object",
            Params={"Bucket": self.bucket, "Key": object_key},
            ExpiresIn=expires_in,
        )

    def exists(self, object_key: str) -> bool:
        try:
            self.client.head_object(Bucket=self.bucket, Key=object_key)
            return True
        except Exception:
            return False
