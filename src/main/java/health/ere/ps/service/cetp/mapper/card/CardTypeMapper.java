package health.ere.ps.service.cetp.mapper.card;

import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.health.service.cetp.domain.eventservice.card.CardType;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMappingConfig.class)
public interface CardTypeMapper {

    CardTypeType toSoap(CardType domain);

    CardType toDomain(CardTypeType soap);
}
