package top.itifrd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.itifrd.utils.AudioUtil;

// @SpringBootTest
class MqttHandleServerApplicationTests {

    @Test
    void contextLoads() {
        try {
            // AudioUtil.AISpeech("好吃 你就多吃点");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
