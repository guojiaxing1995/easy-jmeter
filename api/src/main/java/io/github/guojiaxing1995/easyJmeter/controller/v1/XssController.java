package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.guojiaxing1995.easyJmeter.dto.xss.CreateXssDTO;
import io.github.guojiaxing1995.easyJmeter.service.XssService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/xss")
@Validated
public class XssController {

    @Autowired
    private XssService xssService;

    @PostMapping("/one")
    public CreatedVO xssOne(@RequestBody @Validated CreateXssDTO validator) {

        xssService.createXss(validator);
        return new CreatedVO("获取成功！");
    }

    @PostMapping("")
    public CreatedVO xss(@RequestBody JSONObject jsonObject){

        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            CreateXssDTO xss = new CreateXssDTO();
            xss.setKey(entry.getKey());
            xss.setValue(String.valueOf(entry.getValue()));
            xssService.createXss(xss);
        }

        return new CreatedVO("获取成功！");
    }

    @PostMapping("/constructPermission")
    public JSONObject constructPermission(@RequestBody JSONObject jsonObject){
        String original = xssService.getByKey((String) jsonObject.get("original")).getXssValue();
        String forged = (String) jsonObject.get("forged");
        JSONObject forgedJsonObject = JSON.parseObject(forged);
        // functionList
        JSONArray functionListOriginal = JSON.parseObject(original).getJSONObject("user").getJSONObject("auth").getJSONArray("functionList");
        JSONArray functionListForged = forgedJsonObject.getJSONObject("user").getJSONObject("auth").getJSONArray("functionList");
        for (int i =0;i<functionListOriginal.size();i++){
            JSONObject j = (JSONObject) functionListOriginal.get(i);
            if (j.getString("fullPath").contains("系统管理")){
                functionListForged.add(functionListOriginal.get(i));
            }
        }
        // functionTreeList
        JSONArray functionTreeListOriginal = JSON.parseObject(original).getJSONObject("user").getJSONObject("auth").getJSONArray("functionTreeList");
        JSONArray functionTreeListForged = forgedJsonObject.getJSONObject("user").getJSONObject("auth").getJSONArray("functionTreeList");
        for (int i =0;i<functionTreeListOriginal.size();i++){
            JSONObject j = (JSONObject) functionTreeListOriginal.get(i);
            if (j.getString("text").contains("系统管理")){
                functionTreeListForged.add(functionTreeListOriginal.get(i));
            }
        }
        // new auth
        JSONObject auth = forgedJsonObject.getJSONObject("user").getJSONObject("auth");
        auth.put("functionList", functionListForged);
        auth.put("functionTreeList", functionTreeListForged);
        JSONObject user = forgedJsonObject.getJSONObject("user");
        user.put("auth",auth);
        forgedJsonObject.put("user",user);
        // menu
        JSONArray menuOriginal = JSON.parseObject(original).getJSONObject("permission").getJSONArray("menu");
        JSONArray menuForged = forgedJsonObject.getJSONObject("permission").getJSONArray("menu");
        for (int i =0;i<menuOriginal.size();i++){
            JSONObject j = (JSONObject) menuOriginal.get(i);
            if (j.getString("text").contains("系统管理")){
                menuForged.add(menuOriginal.get(i));
            }
        }
        JSONObject permission = forgedJsonObject.getJSONObject("permission");
        permission.put("menu",menuForged);
        forgedJsonObject.put("permission",permission);

//        String accessToken = JSON.parseObject(forged).getJSONObject("auth").getString("accessToken");
        return forgedJsonObject;
    }
}
