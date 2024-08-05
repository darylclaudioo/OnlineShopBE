package com.onlineshop.OnlineShop_BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = { "com.onlineshop.OnlineShop_BE", "lib.minio", "lib.i18n" })
public class OnlineShopBE {

	public static void main(String[] args) {
		SpringApplication.run(OnlineShopBE.class, args);
	}

}
