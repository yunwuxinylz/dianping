package com.dp.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

public class CaptchaUtils {
    private static final Random RANDOM = new Random();
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // 生成图形验证码图片和文本
    public static Map<String, String> generateCaptcha(int width, int height, int length) {
        Map<String, String> result = new HashMap<>();

        // 创建图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // 设置更好的背景色 - 浅色背景更友好
        g.setColor(new Color(248, 249, 250));
        g.fillRect(0, 0, width, height);

        // 使用更清晰的字体
        g.setFont(new Font("Arial", Font.BOLD, height / 2));

        // 生成随机验证码
        StringBuilder captchaText = new StringBuilder();
        for (int i = 0; i < length; i++) {
            captchaText.append(CAPTCHA_CHARS.charAt(RANDOM.nextInt(CAPTCHA_CHARS.length())));
        }

        // 计算文字位置使其居中显示
        int charWidth = width / (length + 1);

        // 绘制字符 - 使用更鲜明的颜色
        for (int i = 0; i < captchaText.length(); i++) {
            // 使用深色系便于识别
            g.setColor(new Color(
                    RANDOM.nextInt(100),
                    RANDOM.nextInt(100),
                    RANDOM.nextInt(150) + 50));

            // 添加轻微旋转以增加安全性但保持可读性
            int angle = RANDOM.nextInt(30) - 15; // -15到15度的随机旋转

            // 保存当前变换
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(charWidth * i + 15, height / 2 + 10);
            g2d.rotate(Math.toRadians(angle));
            g2d.drawString(String.valueOf(captchaText.charAt(i)), 0, 0);
            g2d.rotate(-Math.toRadians(angle)); // 恢复旋转
            g2d.translate(-(charWidth * i + 15), -(height / 2 + 10)); // 恢复平移
        }

        // 绘制干扰线 - 减少干扰线数量并使用半透明颜色
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(
                    RANDOM.nextInt(100),
                    RANDOM.nextInt(100),
                    RANDOM.nextInt(100),
                    128 // 添加透明度
            ));
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                    RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        // 添加少量噪点而不是过多干扰元素
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(
                    RANDOM.nextInt(255),
                    RANDOM.nextInt(255),
                    RANDOM.nextInt(255),
                    100 // 半透明
            ));
            g.fillRect(RANDOM.nextInt(width), RANDOM.nextInt(height), 2, 2);
        }

        // 将图像转换为Base64字符串
        String base64Image = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        g.dispose();

        result.put("captchaText", captchaText.toString());
        result.put("captchaImage", base64Image);

        return result;
    }
}
