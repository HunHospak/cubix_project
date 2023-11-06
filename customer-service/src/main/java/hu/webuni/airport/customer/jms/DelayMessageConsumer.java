package hu.webuni.airport.customer.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import hu.webuni.airport.ws.DelayMessage;

@Component
public class DelayMessageConsumer {

	@JmsListener(destination = "delays", containerFactory = "myFactory")
	public void onDelayMessage(DelayMessage delayMessage) {
		System.out.println(delayMessage);
	}
	
}
