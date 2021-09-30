package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;

import java.util.Map;

@Component
public class SmartFileMessageBodyDeserializer {

    private Jackson jackson;

    @Autowired
    public SmartFileMessageBodyDeserializer(Jackson jackson) {
        this.jackson = jackson;
    }

    public Object deserialize(String method, Object body) {
        if (body instanceof Map) {
            switch (method) {
                case SendDocument.PATH:
                    return jackson.convertValue(body, SendDocument.class);
                case SendAnimation.PATH:
                    return jackson.convertValue(body, SendAnimation.class);
                case SendAudio.PATH:
                    return jackson.convertValue(body, SendAudio.class);
                case SendVideo.PATH:
                    return jackson.convertValue(body, SendVideo.class);
                case SendVoice.PATH:
                    return jackson.convertValue(body, SendVoice.class);
                case SendSticker.PATH:
                    return jackson.convertValue(body, SendSticker.class);
                case SendPhoto.PATH:
                    return jackson.convertValue(body, SendPhoto.class);
                case SendVideoNote.PATH:
                    return jackson.convertValue(body, SendVideoNote.class);
            }

            throw new IllegalArgumentException("Unsupported method " + method);
        }

        return body;
    }
}
