package demo.hanwix.imageprocessrequestservice.util

import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class HashEncoder {
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
