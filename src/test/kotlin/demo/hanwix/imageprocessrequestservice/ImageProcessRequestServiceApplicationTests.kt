package demo.hanwix.imageprocessrequestservice

import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class ImageProcessRequestServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
