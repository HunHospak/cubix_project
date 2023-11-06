package hu.webuni.airport.web;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.querydsl.core.types.Predicate;

import hu.webuni.airport.api.FlightControllerApi;
import hu.webuni.airport.api.model.FlightDto;
import hu.webuni.airport.mapper.FlightMapper;
import hu.webuni.airport.model.Flight;
import hu.webuni.airport.repository.FlightRepository;
import hu.webuni.airport.service.FlightService;
import hu.webuni.airport.ws.DelayMessage;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FlightController implements FlightControllerApi{
	
	private final NativeWebRequest nativeWebRequest;
	private final FlightService flightService;
	private final FlightRepository flightRepository;
	private final FlightMapper flightMapper;
	private final QuerydslPredicateArgumentResolver prediacateResolver;
	
	private final SimpMessagingTemplate messagingTemplate;
	private final JmsTemplate jmsTemplate;
	
	
	@Override
	public Optional<NativeWebRequest> getRequest() {
		return Optional.of(nativeWebRequest);
	}

	@Override
	public ResponseEntity<FlightDto> createFlight(@Valid FlightDto flightDto) {
		Flight flight = flightService.save(flightMapper.dtoToFlight(flightDto));
		return ResponseEntity.ok(flightMapper.flightToDto(flight));
	}

	@Override
	public ResponseEntity<List<FlightDto>> searchFlights(@Valid FlightDto example) {
		return ResponseEntity.ok(flightMapper.flightsToDtos(flightService.findFlightsByExample(flightMapper.dtoToFlight(example))));
	}

	@Override
	public ResponseEntity<Void> startDelayPolling(Long flightId, Long rate) {
		flightService.startDelayPollingForFlight(flightId, rate);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<Void> stopDelayPolling(Long flightId) {
		flightService.stopDelayPollingForFlight(flightId);
		return ResponseEntity.ok().build();
	}

	public void configurePredicate(@QuerydslPredicate(root = Flight.class) Predicate predicate) {}
	
	@Override
	public ResponseEntity<List<FlightDto>> searchFlights2(@Valid Long id, @Valid String flightNumber,
			@Valid String takeoffIata, @Valid List<String> takeoffTime) {

		Predicate predicate = createPredicate("configurePredicate");
		return ResponseEntity.ok(flightMapper.flightsToDtos(flightRepository.findAll(predicate)));
	}

	private Predicate createPredicate(String configMethodName) {
		Method method;
		try {
			method = this.getClass().getMethod(configMethodName, Predicate.class);
			MethodParameter methodParameter = new MethodParameter(method, 0);
			ModelAndViewContainer mavContainer = null;
			WebDataBinderFactory binderFactory = null;
			return (Predicate) prediacateResolver.resolveArgument(methodParameter, mavContainer, nativeWebRequest, binderFactory);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseEntity<Void> reportDelay(Long id, Integer delay) {
		DelayMessage payload = new DelayMessage(delay, OffsetDateTime.now(), id);
		this.messagingTemplate.convertAndSend("/topic/delay/" + id, payload);
		
		this.jmsTemplate.convertAndSend("delays", payload);
		
		return ResponseEntity.ok().build();
	}

	
	
}
