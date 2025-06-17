package com.myApps.UrlShortner.responsdto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UrlResponseDto {
    String Url;
    String shortUrl;
    Date createdAt;
    Date updatedAt;
    Long accessed;
}
