package ua.com.foxminded.university.domain.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.com.foxminded.university.domain.dto.GroupDto;
import ua.com.foxminded.university.domain.entity.Group;

@Mapper
public interface GroupDtoMapper extends DtoMapper<Group, GroupDto> {

    @Override
    @Mapping(target = "facultyId", source = "faculty.id")
    @Mapping(target = "facultyName", source = "faculty.name")
    GroupDto toDto(Group entity);

    @Override
    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "students", ignore = true)
    Group toEntity(GroupDto dto);

}
