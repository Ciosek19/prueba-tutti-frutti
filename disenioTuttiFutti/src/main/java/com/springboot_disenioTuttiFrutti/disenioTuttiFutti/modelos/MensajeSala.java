package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeSala {
    private String tipo; // JUGADOR_UNIDO, PARTIDA_INICIADA, JUGADOR_LISTO, PARTIDA_FINALIZADA, TIEMPO_ACTUALIZADO, REINICIO_SOLICITADO
    private Long salaId;
    private Object datos; // Puede ser cualquier objeto dependiendo del tipo
}
