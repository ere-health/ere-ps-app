package health.ere.ps.service.cetp.mapper.card;

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.health.service.cetp.domain.eventservice.card.Card;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class, uses = {CardTypeMapper.class, CardVersionMapper.class})
public interface CardMapper {

    Card toDomain(CardInfoType soap);
}
