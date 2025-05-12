package prueba.proyecto;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
public class NotificacionService {
	
    public static void enviarNotificacion(String toEmail, String subject, String messageText) {
        // Configuración del servidor SMTP (en este caso Gmail)
        String host = "smtp.gmail.com";
        String fromEmail = "manuellaraalos@gmail.com"; // Tu correo de envío
        String password = "cvyk igjn ybqq cmvd"; // Tu contraseña (¡Usar un "App Password" si usas Gmail!)

        // Configurar propiedades del correo
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Crear una sesión con la autenticación
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Crear el mensaje
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(messageText, "text/html; charset=utf-8");
            //message.setText(messageText);

            // Enviar el mensaje
            Transport.send(message);

            System.out.println("Correo de notificación enviado exitosamente a: " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Error al enviar el correo de notificación.");
        }
        
    }
    
    public void ejecutarMetodo() {
        String emailDestinatario = "mlaralo3009@g.educaand.es";
        String asunto = "📌 Notificación Importante";
        String cuerpoHtml = "<h2 style='color: #007bff;'>¡Hola!</h2>"
        	    + "<p>Tu tarea está por vencer. Haz clic en el botón para revisarla.</p>"
        	    + "<a href='https://miaplicacion.com/tareas' "
        	    + "style='display: inline-block; padding: 10px 15px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>"
        	    + "Revisar tarea</a>"
        	    + "<br><br>"
        	    + "<img src='https://miaplicacion.com/logo.png' width='100' alt='Mi Aplicación'>"
        	    + "<p style='font-size: 12px; color: gray;'>Este es un correo automático, por favor no respondas.</p>";

        // Enviar la notificación
        enviarNotificacion(emailDestinatario, asunto, cuerpoHtml);
    }
    
    private static final AtomicInteger contador = new AtomicInteger();
    
    @Scheduled(cron = "0 * * * * ?") // Se ejecuta cada hora)
    public void correoCondicion() {
    	
    	if (contador.get() < 3) {
            String emailDestinatario = "mlaralo3009@g.educaand.es";
            String asunto = "📌 Notificación Importante";
            String cuerpoHtml = "<h2 style='color: #007bff;'>¡Hola!</h2>"
            	    + "<p>Tu tarea está por vencer. Haz clic en el botón para revisarla.</p>"
            	    + "<a href='https://miaplicacion.com/tareas' "
            	    + "style='display: inline-block; padding: 10px 15px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>"
            	    + "Revisar tarea</a>"
            	    + "<br><br>"
            	    + "<img src='https://miaplicacion.com/logo.png' width='100' alt='Mi Aplicación'>"
            	    + "<p style='font-size: 12px; color: gray;'>Este es un correo automático, por favor no respondas.</p>";

            // Enviar la notificación
            enviarNotificacion(emailDestinatario, asunto, cuerpoHtml);
            contador.getAndIncrement(); 
            System.out.println(contador);
    	} else {
    		llamada();
    		return;
    	}
    }
    
    private static boolean notificada;
    
    private void llamada() {
    	if (!notificada) {
    		System.out.print("Ya se han enviado 3 notificaciones de correo");
    		notificada = true;
    	} 
    }
    
    private int cont = 0;
    
    //@Scheduled(cron = "0 * * * * ?") // Se ejecuta cada hora)
    public void probar() {
    	System.out.println(cont);
    	cont++;
    }
    
    /*
    public static void main(String[] args) {
        // Ejemplo de uso con HTML
        String emailDestinatario = "mlaralo3009@g.educaand.es";
        String asunto = "📌 Notificación Importante";
        String cuerpoHtml = "<h2 style='color: #007bff;'>¡Hola!</h2>"
        	    + "<p>Tu tarea está por vencer. Haz clic en el botón para revisarla.</p>"
        	    + "<a href='https://miaplicacion.com/tareas' "
        	    + "style='display: inline-block; padding: 10px 15px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>"
        	    + "Revisar tarea</a>"
        	    + "<br><br>"
        	    + "<img src='https://miaplicacion.com/logo.png' width='100' alt='Mi Aplicación'>"
        	    + "<p style='font-size: 12px; color: gray;'>Este es un correo automático, por favor no respondas.</p>";

        // Enviar la notificación
        enviarNotificacion(emailDestinatario, asunto, cuerpoHtml);
    }
    */
    
}
