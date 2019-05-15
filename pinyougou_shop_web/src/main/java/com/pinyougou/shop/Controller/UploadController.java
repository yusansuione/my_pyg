package com.pinyougou.shop.Controller;

import com.pinyougou.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    /**
     * 图片上传
     *
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        try {
            //图片原来的名字
            String originalFilename = file.getOriginalFilename();
            //截取图片的后缀
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //创建fastDFS
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fdfs_client.conf");
            //上传文件到fastDFS
            String file1 = fastDFSClient.uploadFile(file.getBytes(), extName);
            //返回文件的地址
            String url = FILE_SERVER_URL + file1;

            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
