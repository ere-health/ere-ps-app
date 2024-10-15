package health.ere.ps.service.cetp.mapper.status;

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.health.service.cetp.domain.CetpStatus;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class, uses = {ErrorMapper.class})
public interface StatusMapper {

    CetpStatus toDomain(Status soap);
}
