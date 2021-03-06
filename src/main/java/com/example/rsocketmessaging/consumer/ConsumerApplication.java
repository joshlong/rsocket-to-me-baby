package com.example.rsocketmessaging.consumer;

import com.example.rsocketmessaging.Greeting;
import com.example.rsocketmessaging.GreetingRequest;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ConsumerApplication {

	public static void main(String args[]) {
		System.setProperty("spring.profiles.active", "consumer");
		SpringApplication.run(ConsumerApplication.class, args);
	}
}

@Lazy
@Configuration
class ConsumerConfiguration {

	@Bean
	RSocketRequester requester(RSocketStrategies strategies) {
		return RSocketRequester.create(rSocket(), MimeTypeUtils.APPLICATION_JSON, strategies);
	}

	@Bean
	RSocket rSocket() {
		return RSocketFactory
			.connect()
			.dataMimeType(MediaType.APPLICATION_JSON_VALUE)
			.frameDecoder(PayloadDecoder.ZERO_COPY)
			.transport(TcpClientTransport.create(7000))
			.start()
			.block();
	}

}

@Log4j2
@RestController
class GreetingsRestController {

	private final RSocketRequester requester;

	GreetingsRestController(RSocketRequester requester) {
		this.requester = requester;
	}

	@GetMapping("/greet/{name}")
	Publisher<Greeting> greet(@PathVariable String name) {
		return this.requester
			.route("greet")
			.data(new GreetingRequest("Josh"))
			.retrieveFlux(Greeting.class);
	}
}
