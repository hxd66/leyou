package com.leyou.upload.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.upload.config.OSSProperties;
import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadServiceImpl implements UploadService {
    //支持的文件类型
    private static final List<String> suffixes =
            Arrays.asList("image/png", "image/jpeg", "image/bmp");

    @Autowired
    private OSSProperties prop;
    @Autowired
    private OSS client;
    @Override
    public String upload(MultipartFile file) {
        //1、图片信息校验
        //校验文件类型
        String type = file.getContentType();
        if (!suffixes.contains(type)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //校验图片内容
        BufferedImage image = null;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        if (image == null){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //保存图片
        File dir = new File("D:\\JavaSoft\\nginx-1.13.12\\html\\pic");
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(dir,file.getOriginalFilename()));
            //拼接图片地址
            return "http://image.leyou.com/pic/" + file.getOriginalFilename();
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }

    public Map getSignature() {
        //只将demo中try部分保留，报错部分替换参数
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            //***********注意！这里一个坑，前端接收的参数是accessId，demo默认是accessid
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            //***********注意！这是第二个坑！demo的修改上传时间，比较是毫秒不能除1000*********
            respMap.put("expire", String.valueOf(expireEndTime));
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
