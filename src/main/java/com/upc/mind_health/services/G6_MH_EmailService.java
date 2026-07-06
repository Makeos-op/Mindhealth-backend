package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class G6_MH_EmailService {

    private final JavaMailSender mailSender;

    @Value("${mindhealth.mail.remitente:}")
    private String remitente;

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": " + e.getMessage());
        }
    }
}
