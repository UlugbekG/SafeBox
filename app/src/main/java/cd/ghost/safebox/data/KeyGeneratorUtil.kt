package cd.ghost.safebox.data

import android.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyGeneratorUtil {
    companion object {
        fun generateRandomKey(): SecretKey {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            return keyGenerator.generateKey()
        }

        fun encodeKeyToBase64(key: SecretKey): String {
            return Base64.encodeToString(key.encoded, Base64.DEFAULT)
        }
    }
}
