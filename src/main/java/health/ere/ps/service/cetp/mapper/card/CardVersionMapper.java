package health.ere.ps.service.cetp.mapper.card;

import de.health.service.cetp.domain.eventservice.card.CardVersion;
import health.ere.ps.service.cetp.mapper.DefaultMappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMappingConfig.class, uses = {VersionMapper.class})
public interface CardVersionMapper {

    @Mapping(target = "cosVersion", source = "COSVersion")
    @Mapping(target = "atrVersion", source = "ATRVersion")
    @Mapping(target = "gdoVersion", source = "GDOVersion")
    CardVersion toDomain(de.gematik.ws.conn.cardservice.v8.CardInfoType.CardVersion soap);
}
