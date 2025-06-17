package com.myApps.UrlShortner.repository;

import com.myApps.UrlShortner.model.UrlRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface URLRepo extends JpaRepository<UrlRecord,Long > {
    Optional<UrlRecord> findByUrl(String url);
    Optional<UrlRecord> findByshortUrl(String shortUrl);

}
