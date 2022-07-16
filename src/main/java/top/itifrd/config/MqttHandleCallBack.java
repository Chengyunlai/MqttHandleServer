package top.itifrd.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import top.itifrd.utils.AudioUtil;
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
    @Autowired
    private AudioUtil audioUtil;

    private MqttConnectOptions mqttConnectOptions;

    private MqttAsyncClient mqttAsyncClient;

    DecimalFormat df = new DecimalFormat("#.00");

    // 用来存放可视化图标的数据
    private  Map<String,ArrayList> map = new HashMap<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Map<String,Integer> pkLine = new HashMap<>();

    private static Map<String,String> name_gateWay = new HashMap<>();

    //    命令码
    private String valueCode = null;
    //    网关编号
    private String gateWay = null;

    public Map<String, ArrayList> getMap() {
        return this.map;
    }

    static {
        pkLine.put("0101",90);
        pkLine.put("0102",105);
        pkLine.put("0103",115);
        pkLine.put("0104",42);
        pkLine.put("0105",99);
        pkLine.put("0107",82);
        pkLine.put("0108",120);
        pkLine.put("0109",135);
        pkLine.put("0110",168);
        pkLine.put("0111",120);

        name_gateWay.put("0101","水溶性型芯压注机:55-25 [0101]");
        name_gateWay.put("0102","压蜡机:55-100T-31 [0102]");
        name_gateWay.put("0103","合模力250射蜡机:KNC [0103]");
        name_gateWay.put("0104","化蜡机:95-25-20 [0104]");
        name_gateWay.put("0105","(双工位)射蜡机:104-57 [0105]");
        name_gateWay.put("0106","名称暂定 [0106]");
        name_gateWay.put("0107","高压压蜡机:SM55-100-32 [0107]");
        name_gateWay.put("0108","高压压蜡机:SM-55-18 [0108]");
        name_gateWay.put("0109","101组合班 [0109]");
        name_gateWay.put("0110","高压射蜡机SA35-150-4[0110]");
        name_gateWay.put("0111","103组合班 [0111]");
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
        log.error("连接丢失的原因是:{}",cause.toString());
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
        byte[] payload = message.getPayload();
        // log.info("原始数据:{}",payload);
        StringBuilder sb = new StringBuilder();
        for (byte b : payload) {
            // log.info("将数据转换成16进制");
            sb.append(hex10To16(b));
        }
        Date date = new Date();
        String formatDate = simpleDateFormat.format(date);
        // log.info("============》》接收消息主题 : " + topic);
        // log.info("============》》接收消息Qos : " + message.getQos());
        log.info("============》》接收消息内容 : " + sb);
        // log.info("============》》接收ID : " + message.getId());
        log.info("============》》接收数据的时间" + formatDate);
        log.info("接收数据结束 下面可以执行数据处理操作");
        convertValue(sb,formatDate);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    //    十进制转换十六进制
    public static String hex10To16(byte value) {
        String newValue = null;
        try {
            newValue = String.format("%02x", value);
            newValue.substring(newValue.length() - 2, newValue.length());
        }catch (Exception e){
            e.printStackTrace();
            log.error("十进制转十六进制出错");
        }
        return newValue;
    }

    public static float hex16To10(String value) {
        long l = 0;
        try {
            l = Long.parseLong(value, 16);
        }catch (Exception e){
            e.printStackTrace();
            log.error("十六进制转十进制出错");
        }
        return l;
    }

    //    将数据进行转义 + 持久化
    private void convertValue(StringBuilder stringBuilder,String date) {
        // log.info("消息转义体的原始内容:{}",stringBuilder);
        String res = null;
        // valueCode 一般以aa打头 没有问题
        valueCode = stringBuilder.substring(0, 2);
        // log.info("命令码:{}",valueCode);
        // gate 表示网关
        gateWay = stringBuilder.substring(2, 6);
        log.info("网关:{}",gateWay);
        //
        res = stringBuilder.substring(6);
        // log.info("数据体(id+值):{}",res);
        // log.info("数据体的长度:{}",res.length());

        // 下面是模拟的电流数据
        double random = Math.random();
        if (!gateWay.equals("0109") && !gateWay.equals("0111")) {
            ArrayList<Object> tmpValueList = new ArrayList<>();
            String tmpId = "03";
            String tmpName = "电流传感器";
            String tmpValue = df.format(random + 1);
            String tmpDate = date;
            tmpValueList.add(name_gateWay.get(gateWay));
            tmpValueList.add(tmpId);
            tmpValueList.add(tmpName);
            tmpValueList.add(tmpValue);
            tmpValueList.add(tmpDate);
            tmpValueList.add("正常");
            map.put(gateWay + "03", tmpValueList);
        }
        // 数据体当中包含：编号 数值
        for (int dataIndex = 0; dataIndex < res.length(); dataIndex += 6) {
            String id = res.substring(dataIndex, dataIndex + 2);
            // log.info("i , i+2:" +(dataIndex) + "," +(dataIndex+2));
            // log.info("设备的id:{}",id);
            String value = res.substring(dataIndex + 2, dataIndex + 6);
            // log.info("i+2 , i+6:" + (dataIndex+2) +"," +(dataIndex+6));
            // log.info("设备的值:{}",hex16To10(value));
            float resValue = hex16To10(value);
            switch (id){
                case "01":
                    ArrayList<Object> fireList = new ArrayList<>();
                    log.info("编号:"+ id +" 数值:"+ resValue + "，火焰传感器,时间:"+ date);
                    fireList.add(name_gateWay.get(gateWay));
                    fireList.add(id);
                    fireList.add("火焰传感器");
                    fireList.add(resValue);
                    fireList.add(date);
                    if (resValue <= 3840) {
                        fireList.add("--报警--");
                    }else {
                        fireList.add("正常");
                    }
                    map.put(gateWay + id, fireList);
                    if (resValue <= 3840){
                        audioUtil.AISpeech("请注意，火焰传感器探测到异常");
                    }
                    break;
                case "02":
                    resValue *= 0.1;
                    ArrayList<Object> smokeList = new ArrayList<>();
                    log.info("编号:"+ id +" 数值:"+ resValue + "，烟雾传感器,时间:"+ date);
                    // log.info("网关:{}",gateWay);
                    try {
                        smokeList.add(name_gateWay.get(gateWay));
                        smokeList.add(id);
                        smokeList.add("烟雾传感器");
                        smokeList.add(resValue);
                        smokeList.add(date);
                        if (resValue>=pkLine.get(gateWay)) {
                            smokeList.add("--报警--");
                        }else {
                            smokeList.add("正常");
                        }
                        map.put(gateWay + id, smokeList);
                        if (resValue>=pkLine.get(gateWay)){
                            audioUtil.AISpeech("请注意，烟雾传感器探测到异常");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        log.error(e.toString());
                    }
                    break;
                default:
                    log.info("没有匹配上ID值");
            }
        }
    }
}
