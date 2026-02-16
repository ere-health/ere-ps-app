package health.ere.ps.zeta.utils

import de.gematik.zeta.sdk.authentication.smcb.model.ExternalAuthenticateResponse
import de.gematik.zeta.sdk.authentication.smcb.model.ReadCardCertificateResponse
import de.gematik.zeta.sdk.authentication.smcb.model.decodeFromSoap
import nl.adaptivity.xmlutil.serialization.XML

object SoapUtils {

    val xml = XML {
        indentString = ""
        autoPolymorphic = false
    }

    @JvmStatic
    fun deserializeCertificateResponse(body: String): ReadCardCertificateResponse {
        return body.decodeFromSoap(ReadCardCertificateResponse.serializer(), xml)
    }

    @JvmStatic
    fun deserializeAuthenticateResponse(body: String): ExternalAuthenticateResponse {
        return body.decodeFromSoap(ExternalAuthenticateResponse.serializer(), xml)
    }
}