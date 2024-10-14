package health.ere.ps.service.cetp.mapper;

import de.health.service.cetp.domain.fault.Detail;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class)
public interface DetailMapper {

    Detail toDomain(de.gematik.ws.tel.error.v2.Error.Trace.Detail soap);
}
