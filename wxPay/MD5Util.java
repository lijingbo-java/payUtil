package com.jeeplus.modules.marguerite.zongwang.common.WxPay;

import java.security.MessageDigest;

import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * 密码加密
 * @author linzichen
 *
 */
public class MD5Util {

	/**
	 * 返回加密的密码
	 * @param password  原始密码
	 * @param salt  盐
	 * @return
	 */
	public static String passwordEncryption(String password,String salt) {
		String result = new SimpleHash("md5", password, salt, 5).toString();
		return result ;
	}
	
	/**
	 * 加密数据
	 * @param data
	 * @return
	 */
	public static String dataEncryption(String data) {
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(data.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder() ;
			for (byte item : array) {
				sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString().toUpperCase();
		}catch(Exception e){
			
		}
		return null ;
	}
}
