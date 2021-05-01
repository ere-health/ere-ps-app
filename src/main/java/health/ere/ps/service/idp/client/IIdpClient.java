package health.ere.ps.service.idp.client;

import health.ere.ps.model.idp.client.IdpTokenResult;
import health.ere.ps.model.idp.crypto.PkiIdentity;

public interface IIdpClient {

    IdpTokenResult login(PkiIdentity idpIdentity);

    IIdpClient initialize();
}
