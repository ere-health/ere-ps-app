package health.ere.ps.service.cetp.mapper;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.health.service.cetp.domain.CetpStatus;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class, uses = {ErrorMapper.class})
public interface StatusMapper {

    CetpStatus toDomain(Status soap);
}
