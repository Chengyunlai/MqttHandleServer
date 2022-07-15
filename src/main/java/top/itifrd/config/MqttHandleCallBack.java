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

    private  Map<String,ArrayList> map = new HashMap<>();;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Map<String,Integer> pkLine = new HashMap<>();

    private static Map<String,String> name_gateWay = new HashMap<>();

    //    命令码
    private String valueCode = null;
    //    网关编号
    private String gateWay = null;



    public Map<String, ArrayList> getMap() {
        return map;
    }

    static {
        pkLine.put("0101",120);
        pkLine.put("0102",105);
        pkLine.put("0103",115);
        pkLine.put("0104",42);
        pkLine.put("0107",82);
        pkLine.put("0109",135);
        pkLine.put("0110",168);

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
        // 做图形化数据的展示
        byte[] payload = message.getPayload();
        log.info("原始数据:{}",payload);
        StringBuilder sb = new StringBuilder();
        for (byte b : payload) {
            // log.info("将数据转换成16进制");
            sb.append(hex10To16(b));
        }
        Date date = new Date();
        String formatDate = simpleDateFormat.format(date);
        log.info("============》》接收消息主题 : " + topic);
        log.info("============》》接收消息Qos : " + message.getQos());
        log.info("============》》接收消息内容 : " + sb);
        log.info("============》》接收ID : " + message.getId());
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
    public void convertValue(StringBuilder stringBuilder,String date) {
        log.info("消息转义体的原始内容:{}",stringBuilder);
        String res = null;
        // valueCode 一般以aa打头 没有问题
        valueCode = stringBuilder.substring(0, 2);
        log.info("命令码:{}",valueCode);
        // gate 表示网关
        gateWay = stringBuilder.substring(2, 6);
        log.info("网关:{}",gateWay);
        //
        res = stringBuilder.substring(6);
        log.info("数据体(id+值):{}",res);
        log.info("数据体的长度:{}",res.length());

        // 下面是模拟的电流数据
        double random = Math.random();
        if (!"0109".equals(gateWay) || !"0111".equals(gateWay)) {
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
            log.info("i , i+2" +(dataIndex) + " " +(dataIndex+2));
            log.info("设备的id:{}",id);
            String value = res.substring(dataIndex + 2, dataIndex + 6);
            log.info("i+2 , i+6" + (dataIndex+2) +" " +(dataIndex+6));
            log.info("设备的值:{}",value);
            float resValue = hex16To10(value);
            ArrayList<Object> valueList = new ArrayList<>();
            switch (id){
                case "01":
                    log.info("编号:"+ id +" 数值:"+ resValue + "，火焰传感器,时间:"+ date);
                    valueList.add(name_gateWay.get(gateWay));
                    valueList.add(id);
                    valueList.add("火焰传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    if (resValue <= 3840) {
                        valueList.add("--报警--");
                    }else {
                        valueList.add("正常");
                    }
                    map.put(gateWay + id, valueList);
                    if (resValue <= 3840){
                        audioUtil.AISpeech("请注意，火焰传感器探测到异常");
                    }
                    break;
                case "02":
                    resValue *= 0.1;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，烟雾传感器,时间:"+ date);
                    log.info("网关:{}",gateWay);
                    valueList.add(name_gateWay.get(gateWay));
                    valueList.add(id);
                    valueList.add("烟雾传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    valueList.add(resValue>=pkLine.get(gateWay)?"--报警--":"正常");
                    map.put(gateWay + id, valueList);
                    if (resValue>=pkLine.get(gateWay)){
                        audioUtil.AISpeech("请注意，烟雾传感器探测到异常");
                    }
                    break;
                case "03":
//                    灰尘传感器 预处理*0.01
                    resValue *= 0.01;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，灰尘传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("灰尘传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    break;
                case "04":
                    resValue *= 0.0001;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，电流传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("电流传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    break;
                case "05":
                    resValue *= 0.01;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，温度传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("温度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    break;
                case "06":
                    resValue *= 0.01;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，湿度传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("湿度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    break;
                case "07":
                    log.info("编号:"+ id +" 数值:"+ resValue + "，烟雾传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("烟雾传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    System.out.println();
                    break;
                case "09":
                    resValue *= 0.01;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，温度传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("温度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    break;
                case "10":
                    resValue *= 0.01;
                    log.info("编号:"+ id +" 数值:"+ resValue + "，湿度传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("湿度传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
                    break;
                case "11":
                    log.info("编号:"+ id +" 数值:"+ resValue + "，烟雾传感器,时间:"+ date);
                    valueList.add(id);
                    valueList.add("烟雾传感器");
                    valueList.add(resValue);
                    valueList.add(date);
                    map.put(id,valueList);
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
