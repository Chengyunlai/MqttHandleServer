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
        String[] topics = new String[]{"toWeb101","toWeb102","toWeb103","toWeb104","toWeb105","toWeb107","toWeb108","toWeb109","toWeb110"};
        int[] qos = new int[]{0,0,0,0,0,0,0,0,0};
        bean.subscription(topics,qos);
        bean1.run();
    }
}
