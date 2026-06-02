package yayauheny.by.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "encryption")
data class EncryptionProperties(
    val keyset: Keyset = Keyset()
) {
    data class Keyset(
        val json: String = ""
    )
}
