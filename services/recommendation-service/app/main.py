from __future__ import annotations

from uuid import uuid4

from fastapi import FastAPI, Request

from app.api.routes import router
from app.core.config import settings


def create_app() -> FastAPI:
    application = FastAPI(
        title="recommendation-service",
        version="0.1.0",
        docs_url="/docs",
    )

    @application.middleware("http")
    async def request_context_middleware(request: Request, call_next):
        request.state.request_id = request.headers.get("X-Request-Id", f"req_{uuid4()}")
        response = await call_next(request)
        response.headers["X-Request-Id"] = request.state.request_id
        return response

    @application.get("/")
    def root():
        return {
            "service": "recommendation-service",
            "environment": settings.app_environment,
        }

    application.include_router(router)
    return application


app = create_app()
