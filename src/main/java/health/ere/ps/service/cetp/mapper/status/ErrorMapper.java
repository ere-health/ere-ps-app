package health.ere.ps.service.cetp.mapper.status;

import de.health.service.cetp.domain.fault.Error;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMappingConfig.class, uses = {TraceMapper.class})
public interface ErrorMapper {

    @Mapping(source = "messageID", target = "messageId")
    Error toDomain(de.gematik.ws.tel.error.v2.Error soap);
}
