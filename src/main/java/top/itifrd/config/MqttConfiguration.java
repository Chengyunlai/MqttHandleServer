package top.itifrd.config;


import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix="mqtt")
@ToString
@Data
public class MqttConfiguration {
    private String broker;
    private String clientId;
    private String username;
    private String password;
    private int timeout;
    private int KeepAlive;
    private String topics;
    private int qos;
}
