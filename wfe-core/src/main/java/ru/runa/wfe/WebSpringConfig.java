package ru.runa.wfe;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
// @Import({ ChatSocketComponentScan.class })
@ComponentScan("ru.runa.wfe.chat.socket")
public class WebSpringConfig {
}
