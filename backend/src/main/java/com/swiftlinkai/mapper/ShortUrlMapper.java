package com.swiftlinkai.mapper;

import com.swiftlinkai.dto.response.ShortenUrlResponse;
import com.swiftlinkai.entity.ShortUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShortUrlMapper {

    @Mapping(target = "shortUrl", ignore = true)
    @Mapping(target = "aiGenerated", constant = "false")
    @Mapping(source = "generatedTags", target = "tags")
    ShortenUrlResponse toResponse(ShortUrl shortUrl);
}
