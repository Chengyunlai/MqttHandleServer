package top.itifrd;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import top.itifrd.config.MqttSubscription;
import top.itifrd.view.MqttDataView;

import javax.swing.*;


@SpringBootApplication
public class MqttHandleServerApplication extends JFrame {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(MqttHandleServerApplication.class, args);
        MqttSubscription bean = run.getBean(MqttSubscription.class);
        MqttDataView bean1 = run.getBean(MqttDataView.class);
        bean1.run();
        bean.subscription("toWeb",1);
    }
}
