package ru.practicum.events.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.events.category.dto.CategoryDto;
import ru.practicum.events.category.dto.CategoryDtoOut;
import ru.practicum.events.category.model.Category;

@UtilityClass
public class CategoryMapper {

    public static CategoryDtoOut toDto(Category category) {
        return new CategoryDtoOut(category.getId(), category.getName());
    }

    public static Category fromDto(CategoryDto dto) {
        return new Category(null, dto.getName());
    }
}
