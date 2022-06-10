package top.itifrd.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName 构造MqttAsyncClient对象，用于连接 EMQ
 * @Description
 * @Author:chengyunlai
 * @Date
 * @Version 1.0
 **/
@Configuration
@Slf4j
public class MqttAsyncClientConfig {

    // MQTT配置信息
    private MqttConfiguration mqttConfiguration;

    @Autowired
    public void setMqttConfiguration(MqttConfiguration mqttConfiguration) {
        this.mqttConfiguration = mqttConfiguration;
    }

    // MQTT的回调函数
    private MqttHandleCallBack mqttHandleCallBack;

    @Autowired
    public void setMqttHandleCallBack(MqttHandleCallBack mqttHandleCallBack) {
        this.mqttHandleCallBack = mqttHandleCallBack;
    }

    @Bean
    public MqttConnectOptions getOption() {
        //MQTT连接设置
        MqttConnectOptions option = new MqttConnectOptions();
        //设置是否清空session,false表示服务器会保留客户端的连接记录，true表示每次连接到服务器都以新的身份连接
        option.setCleanSession(false);
        //设置连接的用户名
        option.setUserName(mqttConfiguration.getUsername());
        //设置连接的密码
        option.setPassword(mqttConfiguration.getPassword().toCharArray());
        //设置超时时间 单位为秒
        option.setConnectionTimeout(mqttConfiguration.getTimeout());
        //设置会话心跳时间 单位为秒 服务器会每隔(1.5*keepTime)秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        option.setKeepAliveInterval(mqttConfiguration.getKeepAlive());
        //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
        //option.setWill(topic, "close".getBytes(StandardCharsets.UTF_8), 2, true);
        option.setMaxInflight(1000);
        return option;
    }

    @Bean
    public MqttAsyncClient getMqttAsyncClient(){
        MqttAsyncClient mqttAsyncClient = null;
        try {
            // 构造一个与MQTT服务连接的客户端
            mqttAsyncClient = new MqttAsyncClient(mqttConfiguration.getBroker(),mqttConfiguration.getClientId(),new MemoryPersistence());
            MqttConnectOptions options = getOption();
            // 设置回调函数
            mqttAsyncClient.setCallback(mqttHandleCallBack);
            // 连接
            boolean connected = mqttAsyncClient.isConnected();

            log.info("客户端是否已经连接到服务器:{}",connected);
            if (!connected){
                log.info("正在连接Mqtt服务器");
                mqttAsyncClient.connect(options);
                log.info("已经连接至服务器");
            }else {
                mqttAsyncClient.disconnect();
                mqttAsyncClient.connect();
                log.error("断连成功");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return mqttAsyncClient;
    }
}
