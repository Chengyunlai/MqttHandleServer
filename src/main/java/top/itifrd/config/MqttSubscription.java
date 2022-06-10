package top.itifrd.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @ClassName
 * @Description Mqtt的订阅
 * @Author:chengyunlai
 * @Date
 * @Version 1.0
 **/
@Component
@Slf4j
public class MqttSubscription {
    private MqttAsyncClient AsyncClient;

    @Autowired
    public void setAsyncClient(MqttAsyncClient asyncClient) {
        AsyncClient = asyncClient;
    }

    public MqttSubscription() {
        log.info("订阅客户端加载完毕");
    }

    public void subscription(String topic, int qos){
        try {
            AsyncClient.subscribe(topic,qos);
            log.info("订阅成功");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
