package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.MensajeSala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServicio {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void enviarMensajeASala(Long salaId, String tipo, Object datos) {
        MensajeSala mensaje = new MensajeSala(tipo, salaId, datos);
        messagingTemplate.convertAndSend("/topic/sala/" + salaId, mensaje);
    }

    public void notificarActualizacionSalas() {
        messagingTemplate.convertAndSend("/topic/salas", new MensajeSala("SALAS_ACTUALIZADAS", null, null));
    }
}
