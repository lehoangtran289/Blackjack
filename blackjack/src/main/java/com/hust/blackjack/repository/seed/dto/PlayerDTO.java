package com.hust.blackjack.repository.seed.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class PlayerDTO {
    private String playerName;
    private String password;
    private double bank;
}
