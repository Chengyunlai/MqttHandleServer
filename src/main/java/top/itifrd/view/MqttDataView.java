package top.itifrd.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.itifrd.config.MqttHandleCallBack;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * @ClassName
 * @Description
 * @Author:chengyunlai
 * @Date
 * @Version 1.0
 **/
@Configuration
@Slf4j
@EnableScheduling // 定时任务
public class MqttDataView implements Runnable{
    private JTable table =null;
    @Autowired
    private MqttHandleCallBack mqttHandleCallBack;

    @Bean
    public JFrame getDataView(){
        TableColumn column;
        JFrame frame=new JFrame("消息数据表");
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane=frame.getContentPane();
        Map<String, ArrayList> map = mqttHandleCallBack.getMap();
        Set set = map.keySet();
        Object[] arr = set.toArray();
        int row = arr.length;
        table = new JTable(row,6);
        DefaultTableCellRenderer r=new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class,r);
        table.setFont(new Font("新宋体", Font.PLAIN, 26));
        JTableHeader head = table.getTableHeader(); // 创建表格标题对象
        head.setPreferredSize(new Dimension(head.getWidth(), 40));// 设置表头大小
        head.setFont(new Font("楷体", Font.PLAIN, 26));// 设置表格字体
        contentPane.add(new JScrollPane(table));
        frame.setVisible(true);
        return frame;
    }

    @Override
    @Scheduled(cron = "0/2 * * * * ?")
    public void run() {
        Map<String, ArrayList> map = mqttHandleCallBack.getMap();
        Set set=map.keySet();
        Object[] arr=set.toArray();
        Arrays.sort(arr);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        model.setColumnIdentifiers(new Object[]{"名称","设备编号","传感器名称","传感器数值","时间","是否报警"});
        for (int i = 0; i < arr.length; i++) {
            ArrayList list = map.get(arr[i]);
            model.addRow(list.toArray());
        }
        table.setRowHeight(45);

    }
}
