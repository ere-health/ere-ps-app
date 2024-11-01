package health.ere.ps.service.cetp.mapper.event;

import de.gematik.ws.conn.eventservice.v7.Event;
import de.health.service.cetp.domain.eventservice.event.CetpEvent;
import de.health.service.cetp.domain.eventservice.event.mapper.CetpEventMapper;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    config = DefaultMappingConfig.class,
    uses = {EventTypeMapper.class, SeverityTypeMapper.class, ParameterMapper.class}
)
public abstract class EventMapper implements CetpEventMapper<Event> {

    @Override
    @Mapping(target = "subscriptionId", source = "subscriptionID")
    @Mapping(target = "parameters", source = "message.parameter")
    public abstract CetpEvent toDomain(Event soap);
}
