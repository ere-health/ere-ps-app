package health.ere.ps.service.cetp.mapper.card;

import de.gematik.ws.conn.cardservice.v8.VersionInfoType;
import de.health.service.cetp.domain.eventservice.card.VersionInfo;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class)
public interface VersionMapper {

    VersionInfo toDomain(VersionInfoType soap);
}
