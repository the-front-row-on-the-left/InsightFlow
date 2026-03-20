from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_environment: str = Field(default="local", alias="APP_ENVIRONMENT")
    recommendation_port: int = Field(default=8086, alias="RECOMMENDATION_PORT")
    usage_service_base_url: str = Field(default="http://usage-service:8083", alias="USAGE_SERVICE_BASE_URL")
    billing_service_base_url: str = Field(default="http://billing-service:8084", alias="BILLING_SERVICE_BASE_URL")

    model_config = SettingsConfigDict(populate_by_name=True, extra="ignore")


settings = Settings()
