package tech.codingless.standalone.datawork.util;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Base64Utils;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Jwts {

	public static class Playload {

		private String playload;
		private boolean vaild;
		private boolean expired;

		public boolean isExpired() {
			return expired;
		}

		public void setExpired(boolean expired) {
			this.expired = expired;
		}

		/**
		 * 是否有效的
		 * 
		 * @return
		 */
		public boolean isVaild() {
			return vaild;
		}

		public void setVaild(boolean vaild) {
			this.vaild = vaild;
		}

		public String getPlayload() {
			return playload;
		}

		public void setPlayload(String playload) {
			this.playload = playload;
		}

	}

	public static String clearSpecWord(String str) {
		return str.replaceAll("/", "").replaceAll("=", "").replaceAll("-", "").replaceAll("[+]", "").replaceAll("_", "");
	}

	public static Playload decode(String jwt, String hs256Secret) {

		System.out.println("jwt:" + jwt);
		Playload playload = new Playload();
		String[] columns = jwt.split("[.]");
		if (columns.length != 3) {
			playload.setVaild(false);
			return playload;
		}

		String head = columns[0];
		String content = columns[1];

		try {
			// 创建一个HMAC-SHA256的KeySpec
			String al = "HmacSHA256";
			SecretKeySpec keySpec = new SecretKeySpec(hs256Secret.getBytes(), al);

			// 创建一个Mac实例并初始化
			Mac mac = Mac.getInstance(al);
			mac.init(keySpec);
			// 进行哈希运算
			byte[] hmacBytes = mac.doFinal((head + "." + content).getBytes());
			// 字节数组转换为Base64字符串
			String hmacBase64 = new String(Base64Utils.encodeToString(hmacBytes));

			hmacBase64 = clearSpecWord(hmacBase64);// hmacBase64.replaceAll("/", "").replaceAll("=", "").replaceAll("-", "");
			if (hmacBase64.equalsIgnoreCase(clearSpecWord(columns[2]))) {
				playload.setVaild(true);
				String pl = new String(Base64.getUrlDecoder().decode(content));
				playload.setExpired(JSON.parseObject(pl).getLongValue("exp") > System.currentTimeMillis() / 1000);
				playload.setPlayload(pl);
				return playload;
			}
			playload.setVaild(false);
		} catch (Exception e) {
			log.error("jwt parse fail", e);
		}

		return playload;
	}
}
