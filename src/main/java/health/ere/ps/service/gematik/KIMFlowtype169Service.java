package health.ere.ps.service.gematik;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class KIMFlowtype169Service {

    private static Logger log = Logger.getLogger(KIMFlowtype169Service.class.getName());

    public void sendERezeptToKIMAddress(String fromKimAddress, String toKimAddress, String smtpHostServer, String smtpUser, String smtpPassword, String eRezeptToken) {
        try {
            Properties props = System.getProperties();

            props.put("mail.smtp.host", smtpHostServer);
            props.put("mail.smtp.auth", true);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("X-KIM-Dienstkennung", "eRezept;Zuweisung;V1.0");

            msg.setFrom(new InternetAddress(fromKimAddress));

            msg.setReplyTo(InternetAddress.parse(fromKimAddress, false));

            msg.setSubject("E-Rezept direkte Zuweisung", "UTF-8");


            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Sehr geehrte Apotheke,\nim Anhang erhalten Sie den E-Rezept Token für das entsprechende Medikament.\nSchönen Gruß\n", "utf-8");

            MimeBodyPart erezeptTokenPart = new MimeBodyPart();
            erezeptTokenPart.setText(eRezeptToken, "utf8");
            
            Multipart multiPart = new MimeMultipart();
            multiPart.addBodyPart(textPart); // <-- first
            multiPart.addBodyPart(erezeptTokenPart); // <-- second
            msg.setContent(multiPart);

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toKimAddress, false));
            log.info("Message is ready");
            Transport.send(msg);  

            log.info("EMail Sent Successfully!!");
	    } catch (Exception e) {
	      log.log(Level.WARNING, "Error during sending E-Prescription", e);
	    }
    }
}
