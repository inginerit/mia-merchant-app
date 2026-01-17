package md.victoriabank.mia.merchant.utils

import android.content.Context
import android.util.Log
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.RsaKeyUtil
import java.io.InputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class JWTVerifier(private val context: Context) {

    companion object {
        private const val TAG = "JWTVerifier"
    }

    private var publicKey: PublicKey? = null

    init {
        loadPublicKey()
    }

    private fun loadPublicKey() {
        try {
            // Încarcă certificatul din resources
            val certInputStream: InputStream = context.assets.open("vbca.crt")
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificate = certificateFactory.generateCertificate(certInputStream) as X509Certificate
            publicKey = certificate.publicKey
            certInputStream.close()
            Log.d(TAG, "Public key loaded successfully from certificate")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading public key from certificate", e)
        }
    }

    /**
     * Verifică semnătura JWT și returnează payload-ul
     */
    fun verifyAndExtractPayload(jwtToken: String): String? {
        return try {
            if (publicKey == null) {
                Log.e(TAG, "Public key not loaded")
                return null
            }

            val jwtConsumer = JwtConsumerBuilder()
                .setVerificationKey(publicKey)
                .setSkipDefaultAudienceValidation()
                .build()

            val jwtClaims = jwtConsumer.processToClaims(jwtToken)
            val payload = jwtClaims.toJson()
            
            Log.d(TAG, "JWT verified successfully")
            payload
        } catch (e: Exception) {
            Log.e(TAG, "JWT verification failed", e)
            null
        }
    }

    /**
     * Verifică doar semnătura fără să proceseze claims
     */
    fun verifySignature(jwtToken: String): Boolean {
        return try {
            if (publicKey == null) {
                Log.e(TAG, "Public key not loaded")
                return false
            }

            val jws = JsonWebSignature()
            jws.compactSerialization = jwtToken
            jws.key = publicKey
            
            val isValid = jws.verifySignature()
            Log.d(TAG, "JWT signature verification: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying JWT signature", e)
            false
        }
    }

    /**
     * Extrage payload-ul fără verificare (doar pentru debugging)
     */
    fun extractPayloadWithoutVerification(jwtToken: String): String? {
        return try {
            val parts = jwtToken.split(".")
            if (parts.size != 3) {
                Log.e(TAG, "Invalid JWT format")
                return null
            }

            val payload = parts[1]
            val decodedBytes = Base64.getUrlDecoder().decode(payload)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting payload", e)
            null
        }
    }
}
