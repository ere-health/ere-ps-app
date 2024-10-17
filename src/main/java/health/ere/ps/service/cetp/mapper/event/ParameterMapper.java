package health.ere.ps.service.cetp.mapper.event;

import de.gematik.ws.conn.eventservice.v7.Event;
import de.health.service.cetp.domain.eventservice.event.CetpParameter;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class)
public interface ParameterMapper {

    CetpParameter toDomain(Event.Message.Parameter soap);
}
