package com.myApps.UrlShortner.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String url;
    private String shortUrl;
    @CreatedDate
    private Date generatedAt;
    @LastModifiedDate
    private Date updatedAt;
    private Long accessed;

    public void incrementAccessed(Long accessed){
        this.accessed++;
    }
}
