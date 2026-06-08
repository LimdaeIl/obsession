package com.app.obsession;

import com.app.obsession.global.redis.RedisKeyProperties;
import com.app.obsession.global.security.jwt.JwtProperties;
import com.app.obsession.payment.infrastructure.external.TossPaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
		JwtProperties.class,
		RedisKeyProperties.class,
		TossPaymentProperties.class
})
@SpringBootApplication
public class ObsessionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObsessionApplication.class, args);
	}

}
