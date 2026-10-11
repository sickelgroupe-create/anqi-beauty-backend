package com.ruoyi.web.controller.system;

import java.util.Set;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.alibaba.fastjson2.JSON;

/** Unified public gateway and permission-controlled business administration. */
@RestController
public class AnqiController {
 @Value("${anqi.internal-secret:}") private String secret;
 @Value("${anqi.business-url:http://127.0.0.1:3016}") private String businessUrl;
 private final RestTemplate http;
 public AnqiController(){SimpleClientHttpRequestFactory f=new SimpleClientHttpRequestFactory();f.setConnectTimeout(3000);f.setReadTimeout(30000);http=new RestTemplate(f);}

 @Anonymous
 @RequestMapping("/api/**")
 public ResponseEntity<String> customer(HttpServletRequest req,@RequestBody(required=false) String body){
  String uri=req.getRequestURI();
  if(!uri.matches("/api/(health|bootstrap|auth/(wechat|refresh|logout)|activities|alliances|me(/avatar)?|share-code|sign|rewards|ledger|redemptions|orders(/[a-zA-Z0-9-]+(/(pay|cancel))?)?|addresses(/[a-zA-Z0-9-]+)?|appointments|claims|shares|earnings)"))return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"接口不存在\"}");
  return forward(req,uri,body,false);
 }

 @RequestMapping({"/anqi/{resource}","/anqi/{resource}/{id}"})
 @Log(title="暨美丽业务管理",businessType=BusinessType.OTHER,isSaveRequestData=false,isSaveResponseData=false)
 public Object admin(@PathVariable String resource,@PathVariable(required=false) String id,HttpServletRequest req,@RequestBody(required=false) String body){
  if(!resource.matches("dashboard|config|upload|products|stores|users|orders|appointments|addresses|claims|activities|alliances|shares|earnings|rewards|ledger|redemptions|audit|paymentAudits|categories|homeModules|assets"))return AjaxResult.error("业务资源不存在");
  if(id!=null&&!id.matches("[a-zA-Z0-9-]+"))return AjaxResult.error("编号无效");
  String action="GET".equals(req.getMethod())?"list":"edit";
  String permissionResource=(resource.equals("homeModules")||resource.equals("assets"))?"config":resource;
  Set<String> permissions=SecurityUtils.getLoginUser().getPermissions();
  if(!SecurityUtils.isAdmin(SecurityUtils.getUserId())&&!permissions.contains("*:*:*")&&!permissions.contains("anqi:"+permissionResource+":"+action))return AjaxResult.error(403,"无此业务操作权限");
  if(secret==null||secret.trim().isEmpty())return AjaxResult.error(503,"业务服务未配置内部密钥");
  ResponseEntity<String> r=forward(req,"/api/admin/"+resource+(id==null?"":"/"+id),body,true);
  if(!r.getStatusCode().is2xxSuccessful()){
   String message="业务服务请求失败";
   try{if(r.getBody()!=null&&!r.getBody().trim().isEmpty())message=Objects.toString(JSON.parseObject(r.getBody()).getString("error"),message);}catch(RuntimeException ignored){}
   return AjaxResult.error(r.getStatusCodeValue(),message);
  }
  Object value;
  try{value=JSON.parse(r.getBody());}catch(RuntimeException e){return AjaxResult.error(502,"业务服务返回了无效响应");}
  return AjaxResult.success(value);
 }

 private ResponseEntity<String> forward(HttpServletRequest req,String uri,String body,boolean admin){
  HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);
  if(req.getHeader("Authorization")!=null)headers.set("Authorization",req.getHeader("Authorization"));
  if(req.getHeader("Idempotency-Key")!=null)headers.set("Idempotency-Key",req.getHeader("Idempotency-Key"));
  headers.set("X-Real-IP",IpUtils.getIpAddr(req));
  if(admin){headers.set("X-Anqi-Internal",secret);headers.set("X-Anqi-Actor",SecurityUtils.getUsername());}
  try {ResponseEntity<String> r=http.exchange(businessUrl.replaceAll("/+$","")+uri,HttpMethod.valueOf(req.getMethod()),new HttpEntity<>(body,headers),String.class);return ResponseEntity.status(r.getStatusCode()).contentType(MediaType.APPLICATION_JSON).cacheControl(CacheControl.noStore()).body(r.getBody());}
  catch(HttpStatusCodeException e){return ResponseEntity.status(e.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(e.getResponseBodyAsString());}
  catch(RestClientException e){return ResponseEntity.status(503).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"业务服务暂不可用\"}");}
 }
}



