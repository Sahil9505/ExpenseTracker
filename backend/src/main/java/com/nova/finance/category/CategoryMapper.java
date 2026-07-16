package com.nova.finance.category;

import com.nova.finance.category.web.dto.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "type", source = "type")
    CategoryResponse toResponse(Category category);
}
