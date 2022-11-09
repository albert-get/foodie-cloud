package com.lenchif.user.controller.center;


import com.lenchif.controller.BaseController;
import com.lenchif.pojo.JSONResult;
import com.lenchif.user.pojo.Users;
import com.lenchif.user.pojo.bo.center.CenterUserBO;
import com.lenchif.user.resource.FileUpload;
import com.lenchif.user.service.center.CenterUserService;
import com.lenchif.utils.CookieUtils;
import com.lenchif.utils.DateUtil;
import com.lenchif.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "用户信息接口", tags = {"用户信息相关接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "修改用户信息", notes = "修改用户信息", httpMethod = "POST")
    @PostMapping("/update")
    public JSONResult update(@ApiParam(name = "userId", value = "用户id", required = true)
                                 @RequestParam String userId,
                             @RequestBody @Valid CenterUserBO centerUserBO,
                             BindingResult result,
                             HttpServletRequest request, HttpServletResponse response){

        // 判断BindingResult是否保存错误的验证信息，如果有，则直接return
        if(result.hasErrors()){
            Map<String, String> errors = getErrors(result);
            return JSONResult.errorMap(errors);
        }

        Users userResult = centerUserService.updateUserInfo(userId, centerUserBO);

        userResult = setNullProperty(userResult);
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 后续要改，增加令牌token，会整合进redis，分布式会话

        return JSONResult.ok();

    }

    private Map<String, String> getErrors(BindingResult result){
        HashMap<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for(FieldError e:errorList){
            String name = e.getField();
            String message = e.getDefaultMessage();
            map.put(name, message);
        }
        return map;
    }

    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    @ApiOperation(value = "用户头像修改", notes = "用户头像修改", httpMethod = "POST")
    @PostMapping("/uploadFace")
    public JSONResult uploadFace(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "file", value = "用户头像", required = true)
            MultipartFile file,
            HttpServletRequest request, HttpServletResponse response
    ){

        // 定义头像保存的地址
//        String fileSpace = IMAGE_USER_FACE_LOCATION;
        String fileSpace=fileUpload.getImageUserFaceLocation();
        // 在路径上为每一个用户增加一个userid，用于区分不同用户上传
        String uploadPathPrefix = File.separator + userId;

        if(file!=null){
            FileOutputStream fileOutputStream = null;

            try {
                String filename = file.getOriginalFilename();
                if(StringUtils.isNoneBlank(filename)){
                    String[] splitName = filename.split("\\.");
                    String suffix = splitName[splitName.length - 1];
                    if(!suffix.equalsIgnoreCase("png")&&
                            !suffix.equalsIgnoreCase("jpg")&&
                            !suffix.equalsIgnoreCase("jpeg")){
                        return JSONResult.errorMsg("图片格式不正确");
                    }

                    String newFileName = "face-" + userId + "." + suffix;
                    String finalFilePath = fileSpace + uploadPathPrefix + File.separator + newFileName;

                    // 用于提供给web服务访问的地址
                    uploadPathPrefix += ("/" + newFileName);

                    File outFile = new File(finalFilePath);
                    if(outFile.getParentFile()!=null){
                        outFile.getParentFile().mkdirs();
                    }
                    fileOutputStream=new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream,fileOutputStream);

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {

                try {
                    if(fileOutputStream!=null){
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }else {
            return JSONResult.errorMsg("文件不能为空");
        }

        String imageServerUrl = fileUpload.getImageServerUrl();
        String visitUrl = imageServerUrl + uploadPathPrefix + "?t=" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);
        Users users = centerUserService.updateUserFace(userId, visitUrl);
        users = setNullProperty(users);

        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(users), true);

        // TODO 后续要改，增加令牌token，会整合进redis，分布式会话

        return JSONResult.ok();
    }
}
