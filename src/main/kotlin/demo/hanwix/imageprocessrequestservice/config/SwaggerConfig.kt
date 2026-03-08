package demo.hanwix.imageprocessrequestservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(Info()
            .title("Image Process Request Service")
            .description("이미지 처리 요청 서비스 API")
            .version("v1.0.0"))
}
