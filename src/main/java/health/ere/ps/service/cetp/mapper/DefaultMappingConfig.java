package health.ere.ps.service.cetp.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "jakarta",
    uses = {MapperUtils.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE
)
public interface DefaultMappingConfig {
}
