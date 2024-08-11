package com.drinkeg.drinkeg.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WineNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String cardNewsImg;

    public WineNews updateTitle(String title) {
        this.title = title;
        return this;
    }
    public WineNews updateCardNewsImg(String cardNewsImg) {
        this.cardNewsImg = cardNewsImg;
        return this;
    }
}
