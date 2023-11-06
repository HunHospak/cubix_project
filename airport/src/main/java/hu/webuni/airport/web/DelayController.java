package hu.webuni.airport.web;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import hu.webuni.airport.service.DelayService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DelayController {

	private final DelayService delayService;
	
	@GetMapping("/api/flights/{id}/delay")
	@Async
	public CompletableFuture<Integer> getDelayForFlight(@PathVariable long id) {
		System.out.println("DelayService.getDelayForFlight called at thread " + Thread.currentThread().getName());

		return CompletableFuture.completedFuture(delayService.getDelay(id));
	}
}
