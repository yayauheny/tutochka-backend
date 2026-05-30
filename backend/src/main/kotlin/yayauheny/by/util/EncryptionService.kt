package yayauheny.by.util

import com.google.crypto.tink.DeterministicAead
import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.daead.DeterministicAeadConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class EncryptionService(
    @Value("\${encryption.keyset.json:\#{null}}") keysetJson: String?
) {
    private val deterministicAead: DeterministicAead = createPrimitive(keysetJson)

    fun encrypt(value: String): String {
        val encrypted =
            deterministicAead.encryptDeterministically(
                value.toByteArray(Charsets.UTF_8),
                EMPTY_ASSOCIATED_DATA
            )
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(value: String): String {
        val decrypted =
            deterministicAead.decryptDeterministically(
                Base64.getDecoder().decode(value),
                EMPTY_ASSOCIATED_DATA
            )
        return decrypted.toString(Charsets.UTF_8)
    }

    private fun createPrimitive(keysetJson: String?): DeterministicAead {
        require(!keysetJson.isNullOrBlank()) { "encryption.keyset.json is required" }
        ensureRegistered()
        val handle = TinkJsonProtoKeysetFormat.parseKeyset(keysetJson, InsecureSecretKeyAccess.get())
        return handle.getPrimitive(RegistryConfiguration.get(), DeterministicAead::class.java)
    }

    companion object {
        private val EMPTY_ASSOCIATED_DATA = byteArrayOf()

        @Volatile
        private var registered: Boolean = false

        private fun ensureRegistered() {
            if (!registered) {
                synchronized(EncryptionService::class.java) {
                    if (!registered) {
                        DeterministicAeadConfig.register()
                        registered = true
                    }
                }
            }
        }
    }
}
