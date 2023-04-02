package com.douding.file.controller.admin;

import com.douding.server.domain.Test;
import com.douding.server.dto.FileDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.enums.FileUseEnum;
import com.douding.server.service.FileService;
import com.douding.server.service.TestService;
import com.douding.server.util.Base64ToMultipartFile;
import com.douding.server.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/*
    返回json 应用@RestController
    返回页面  用用@Controller
 */
@RequestMapping("/admin/file")
@RestController
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);
    public  static final String BUSINESS_NAME ="文件上传";
    @Resource
    private TestService testService;

    @Value("${file.path}")
    private String FILE_PATH;

    @Value("${file.domain}")
    private String FILE_DOMAIN;

    @Resource
    private FileService fileService;
    @PostMapping("/upload")
    public ResponseDto uploadOfMerge(@RequestBody FileDto fileDto) throws IOException {
        String use = fileDto.getUse();
        String key = fileDto.getKey();
        String suffix = fileDto.getSuffix();
        String shardBase64 = fileDto.getShard();
        MultipartFile shard = Base64ToMultipartFile.base64ToMultipart(fileDto.getShard());
        System.out.println(shard);
        FileUseEnum useEnum = FileUseEnum.getByCode(use);
        String dir = useEnum.name().toLowerCase();
        File fullDir = new File(FILE_PATH + dir);
        if (!fullDir.exists()) {
            fullDir.mkdir();
        }
        String path = new StringBuffer(dir)
                .append('/')
                .append(key)
                .append(".")
                .append(suffix).toString();
        String localPath = new StringBuffer(path)
                .append(".")
                .append(fileDto.getShardIndex()).toString();
        String fullPath = FILE_PATH + localPath;
        File dest = new File(fullPath);
        shard.transferTo(dest);
        fileDto.setPath(path);
        fileService.save(fileDto);
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(fileDto);
        // 4. 合并
        // 若分片均已上传，将所有分片合并成一个文件。
        if (fileDto.getShardIndex().equals(fileDto.getShardTotal())) {
            this.merge(fileDto);
        }
        // 5. 返回分片上传结果
        return responseDto;
    }

    private void merge(FileDto fileDto) {
        String path = fileDto.getPath();
        Integer shardTotal = fileDto.getShardTotal();
        File newFile = new File(FILE_PATH + path);
        byte[] byt = new byte[10 * 1024 * 1024];
        FileInputStream inputStream = null;   // 分片文件
        int len;
        // 文件追加写入
        try (FileOutputStream outputStream = new FileOutputStream(newFile, true);
        ) {
            for (int i = 0; i < shardTotal; i++) {
                // 读取第一个分片
                inputStream = new FileInputStream(new File(FILE_PATH + path + "." + (i+1))); // course\6sfSqfOwzmik4A4icMYuUe.mp4.1
                while ((len = inputStream.read(byt))!=-1) {
                    outputStream.write(byt, 0, len);
                }
                inputStream.close();
            }
        } catch (Exception e) {
        }
        // 删除分片
        for (int i = 0; i < shardTotal; i++) {
            String filePath = FILE_PATH + path + "." + (i + 1);
            System.out.println(filePath);
            File file = new File(filePath);
            boolean result = file.delete();
            System.out.println(result);
        }
    }
    @GetMapping("/check/{key}")
    public ResponseDto check(@PathVariable String key) {
        ResponseDto responseDto = new ResponseDto();
        FileDto fileDto = fileService.findByKey(key);
        responseDto.setContent(fileDto);
        return responseDto;
    }


}//end class
