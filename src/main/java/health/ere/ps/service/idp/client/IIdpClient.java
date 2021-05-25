package health.ere.ps.service.idp.client;

import health.ere.ps.exception.idp.IdpException;
import health.ere.ps.exception.idp.IdpClientException;
import health.ere.ps.exception.idp.IdpJoseException;
import health.ere.ps.exception.idp.crypto.IdpCryptoException;
import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;

public interface IIdpClient {

    IdpTokenResult login(PkiIdentity idpIdentity) throws IdpException, IdpClientException, IdpJoseException, IdpCryptoException;

    IIdpClient initializeClient() throws IdpClientException, IdpException, IdpCryptoException, IdpJoseException;
}
