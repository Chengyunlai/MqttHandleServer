package top.itifrd;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import top.itifrd.config.MqttSubscription;
import top.itifrd.utils.AudioUtil;
import top.itifrd.view.MqttDataView;

import javax.swing.*;


@SpringBootApplication
public class MqttHandleServerApplication extends JFrame {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(MqttHandleServerApplication.class, args);
        MqttSubscription bean = run.getBean(MqttSubscription.class);
        MqttDataView bean1 = run.getBean(MqttDataView.class);
        bean1.run();
        String[] topics = new String[]{"toWeb101","toWeb102","toWeb103","toWeb104","toWeb107","toWeb109","toWeb110"};
        int[] qos = new int[]{1,1,1,1,1,1,1};
        // String[] topics = new String[]{"toWeb"};
        // int[] qos = new int[]{1};
        bean.subscription(topics,qos);
    }
}
