package api.transsaction.presentation.mapper;

import api.transsaction.domain.Transsaction;
import api.transsaction.presentation.model.TranssactionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TranssactionMapper
{
    Transsaction modelToEntity (TranssactionModel model);
    TranssactionModel entityToModel(Transsaction event);
    @Mapping(target = "id", ignore=true)
    void update(@MappingTarget Transsaction entity, Transsaction updateEntity);
}
