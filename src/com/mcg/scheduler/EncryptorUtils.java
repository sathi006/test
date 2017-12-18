package com.mcg.scheduler;


import org.jasypt.util.text.BasicTextEncryptor;

public class EncryptorUtils {
	private static final String MASTER_KEY = System.getProperty("MASTER_KEY", "master-key");

	private static BasicTextEncryptor encryptor = new BasicTextEncryptor();

	static {
		encryptor.setPassword(MASTER_KEY);
	}

	public static final String encryptPassword(String plainPassword) {
		return encryptor.encrypt(plainPassword);
	}

	public static final String decryptPassword(String encryptedPassword) {
		return encryptor.decrypt(encryptedPassword);
	}
}