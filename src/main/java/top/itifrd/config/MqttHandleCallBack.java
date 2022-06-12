package top.itifrd.config;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import top.itifrd.utils.AudioUtil;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName
 * @Description
 * @Author:chengyunlai
 * @Date
 * @Version 1.0
 **/
@Slf4j
@Component
public class MqttHandleCallBack implements MqttCallback {

    private AudioUtil audioUtil;

    private MqttConnectOptions mqttConnectOptions;

    private MqttAsyncClient mqttAsyncClient;

    DecimalFormat df = new DecimalFormat("#.00");

    private Map<String, ArrayList> map = new HashMap<>();
    ;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //    命令码
    private String valueCode = null;
    //    网关编号
    private String gateWay = null;


    public Map<String, ArrayList> getMap() {
        return map;
    }

    @Autowired
    public void setAudioUtil(AudioUtil audioUtil) {
        this.audioUtil = audioUtil;
    }

    @Autowired
    @Lazy
    public void setMqttConnectOptions(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }

    @Autowired
    @Lazy
    public void setMqttAsyncClient(MqttAsyncClient mqttAsyncClient) {
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("连接丢失的原因是:{}", cause.toString());
        // 丢失重连
        log.info("正在尝试重新连接");
        try {
            mqttAsyncClient.connect(mqttConnectOptions);
            log.info("重连成功");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("收到数据");
        // 做图形化数据的展示
        byte[] payload = message.getPayload();
        StringBuilder sb = new StringBuilder();
        for (byte b : payload) {
            sb.append(hex10To16(b));
        }

        Date date = new Date();
        String formatDate = simpleDateFormat.format(date);
        convertValue(sb, formatDate);

        log.info("============》》接收消息主题 : " + topic);
        log.info("============》》接收消息Qos : " + message.getQos());
        log.info("============》》接收消息内容 : " + sb);
        log.info("============》》接收ID : " + message.getId());
        log.info("============》》接收数据的时间" + formatDate);
        log.info("接收数据结束 下面可以执行数据处理操作");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    //    十进制转换十六进制
    public static String hex10To16(byte value) {
        String newValue = String.format("%02x", value);
        return newValue.substring(newValue.length() - 2, newValue.length());
    }

    public static float hex16To10(String value) {
        return Long.parseLong(value, 16);
    }

    //    将数据进行转义 + 持久化
    public void convertValue(StringBuilder stringBuilder, String date) {
//        Map<Integer,String> map = new HashMap<>();
//         String res = null;
        valueCode = stringBuilder.substring(0, 2);
        gateWay = stringBuilder.substring(2, 6);
        // res = stringBuilder.substring(6);
        log.info("命令码:" + valueCode);
        log.info("网关:" + gateWay);

        // 数据体当中包含：编号 数值
        for (int i = 6; i < stringBuilder.length(); i += 6) {
            String id = stringBuilder.substring(i, i + 2);
            String value = stringBuilder.substring(i + 2, i + 6);
            float resValue = hex16To10(value);

            ArrayList<Object> valueList = new ArrayList<>();
            double random = Math.random();
            if (!gateWay.equals("0109")) {
                ArrayList<Object> tmpValueList = new ArrayList<>();
                String tmpId = "03";
                String tmpName = "电流传感器";
                String tmpValue = df.format(random + 1);
                String tmpDate = date;
                tmpValueList.add(gateWay);
                tmpValueList.add(tmpId);
                tmpValueList.add(tmpName);
                tmpValueList.add(tmpValue);
                tmpValueList.add(tmpDate);
                tmpValueList.add("正常");
                map.put(gateWay + "03", tmpValueList);
            }
            switch (id) {
                case "01":
                    log.info("编号:" + id + " 数值:" + resValue + "，火焰传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("火焰传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add(resValue <= 3840 ? "报警" : "正常");
                    map.put(gateWay + id, valueList);
                    if (resValue <= 3840) {
                        try {
                            audioUtil.AISpeech("请注意，火焰传感器探测到异常");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "02":
//                    一氧化碳 先预处理 * 0.1
//                     resValue *= 0.1;
                    log.info("编号:" + id + " 数值:" + resValue + "，烟雾传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("烟雾传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add(resValue >= 1792 ? "报警" : "正常");
                    map.put(gateWay + id, valueList);
                    if (resValue >= 1792) {
                        try {
                            audioUtil.AISpeech("请注意，烟雾传感器探测到异常");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case "03":
//                    灰尘传感器 预处理*0.01
//                     resValue *= 0.0001;
                    log.info("编号:" + id + " 数值:" + resValue + "，电流传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("电流传感器");
                    valueList.add(df.format(Math.random() + 1));
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    break;
                case "04":
                    resValue *= 0.0001;
                    log.info("编号:" + id + " 数值:" + resValue + "，电流传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("电流传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    break;
                case "05":
                    resValue *= 0.01;
                    log.info("编号:" + id + " 数值:" + resValue + "，温度传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("温度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    break;
                case "06":
                    resValue *= 0.01;
                    log.info("编号:" + id + " 数值:" + resValue + "，湿度传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("湿度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    break;
                case "07":
                    log.info("编号:" + id + " 数值:" + resValue + "，烟雾传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("烟雾传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    System.out.println();
                    break;
                case "09":
                    resValue *= 0.01;
                    log.info("编号:" + id + " 数值:" + resValue + "，温度传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("温度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    if (resValue>20){
                        try {
                            audioUtil.AISpeech("温度传感器探测到异常");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "10":
                    resValue *= 0.01;
                    log.info("编号:" + id + " 数值:" + resValue + "，湿度传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("湿度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    // if (resValue > 60) {
                    //     try {
                    //         audioUtil.AISpeech("湿度传感器探测到异常");
                    //     } catch (Exception e) {
                    //         e.printStackTrace();
                    //     }
                    // }
                    break;
                case "11":
                    log.info("编号:" + id + " 数值:" + resValue + "，烟雾传感器,时间:" + date);
                    valueList.add(gateWay);
                    valueList.add(id);
                    valueList.add("烟雾传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add("-");
                    map.put(gateWay + id, valueList);
                    break;
            }
            // int intId = Integer.parseInt(id);

//            创建一个pojo对象 并进行数据持久化
//             SensingElementValueList sensingElementValueList = new SensingElementValueList(intId,resValue,date);
// //            System.out.println(sensingElementValueList);
//             SqlSession session = MybatisUtils.getSession();
//             SensingElementValueListMapper mapper = session.getMapper(SensingElementValueListMapper.class);
//             int intRes = mapper.addSensingElementValue(sensingElementValueList);
//             session.commit();
//             System.out.println("插入结果:"+intRes);
        }

//        return map;
    }
}
