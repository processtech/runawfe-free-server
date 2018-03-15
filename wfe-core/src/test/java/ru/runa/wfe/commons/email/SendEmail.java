package ru.runa.wfe.commons.email;

import ru.runa.wfe.commons.email.EmailConfig.Attachment;

import com.google.common.base.Charsets;

public class SendEmail {
    static {
        System.setProperty("mail.mime.encodefilename", "true");
    }

    public static void main(String[] args) throws Exception {
        EmailConfig config = new EmailConfig();
        config.getConnectionProperties().put("mail.transport.protocol", "smtp");
        config.getConnectionProperties().put("mail.host", "localhost");
        config.getConnectionProperties().put("mail.smtp.port", "25");
        config.getHeaderProperties().put("To", "dofs197@mail.ru");
        config.getHeaderProperties().put("Subject", "СУТС2.0. Тест аттача");
        config.setMessage("Тестовое сообщение");
        Attachment attachment = new Attachment();
        attachment.fileName = "Поручение об изменении купюрности кассет, очень и очень длинное, по некоторое заявке.xlsx";
        attachment.content = "Текстовый file".getBytes(Charsets.UTF_8);
        config.getAttachments().add(attachment);
        EmailUtils.sendMessage(config);
    }

}
