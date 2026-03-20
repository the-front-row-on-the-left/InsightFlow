from __future__ import annotations

from typing import Generic, TypeVar
from uuid import uuid4

from pydantic import BaseModel, Field


T = TypeVar("T")


class MetaResponse(BaseModel):
    request_id: str = Field(alias="request_id")


class ApiResponse(BaseModel, Generic[T]):
    success: bool
    data: T
    meta: MetaResponse


def wrap_success(data: T, request_id: str | None = None) -> ApiResponse[T]:
    return ApiResponse(
        success=True,
        data=data,
        meta=MetaResponse(request_id=request_id or f"req_{uuid4()}"),
    )
