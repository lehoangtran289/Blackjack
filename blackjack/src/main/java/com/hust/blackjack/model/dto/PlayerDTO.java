package com.hust.blackjack.model.dto;

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
