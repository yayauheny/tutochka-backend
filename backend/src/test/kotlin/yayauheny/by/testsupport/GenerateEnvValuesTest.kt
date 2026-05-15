package yayauheny.by.testsupport

import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.daead.DeterministicAeadConfig
import com.google.crypto.tink.daead.PredefinedDeterministicAeadParameters
import java.security.SecureRandom
import java.util.Base64
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Environment Value Generators")
class GenerateEnvValuesTest {
    @Test
    @DisplayName("Prints a fresh ENCRYPTION_KEYSET_JSON")
    fun prints_encryption_keyset_json() {
        DeterministicAeadConfig.register()

        val keysetHandle =
            KeysetHandle.generateNew(
                PredefinedDeterministicAeadParameters.AES256_SIV
            )

        val json =
            TinkJsonProtoKeysetFormat.serializeKeyset(
                keysetHandle,
                InsecureSecretKeyAccess.get()
            )

        println("ENCRYPTION_KEYSET_JSON=$json")
    }

    @Test
    @DisplayName("Prints a fresh BOT_WEBHOOK_SECRET_TOKEN")
    fun prints_bot_webhook_secret_token() {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)

        val secretToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        println("BOT_WEBHOOK_SECRET_TOKEN=$secretToken")
    }
}
