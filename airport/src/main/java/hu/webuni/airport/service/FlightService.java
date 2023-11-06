package hu.webuni.airport.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

import hu.webuni.airport.aspect.LogCall;
import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.Flight;
import hu.webuni.airport.model.QFlight;
import hu.webuni.airport.repository.AirportRepository;
import hu.webuni.airport.repository.FlightRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@LogCall
public class FlightService {
	
	private final AirportRepository airportRepository;
	private final FlightRepository flightRepository;
	private final DelayService delayService;
	
	private final TaskScheduler taskScheduler;
	
	private Map<Long, ScheduledFuture<?>> delayPollerJobs = new ConcurrentHashMap<>();
	
	@Transactional
	public Flight save(Flight flight) {
		//a takeoff/landing airportból csak az id-t vesszük figyelembe, már létezniük kell
		flight.setTakeoff(airportRepository.findById(flight.getTakeoff().getId()).get());
		flight.setLanding(airportRepository.findById(flight.getLanding().getId()).get());
		return flightRepository.save(flight);
	}

//	public List<Flight> findFlightsByExample(Flight example) {
//
//		long id = example.getId();
//		String flightNumber = example.getFlightNumber();
//		String takeoffIata = null;
//		Airport takeoff = example.getTakeoff();
//		if (takeoff != null)
//			takeoffIata = takeoff.getIata();
//		LocalDateTime takeoffTime = example.getTakeoffTime();
//
//		Specification<Flight> spec = Specification.where(null);
//
//		if (id > 0) {
//			spec = spec.and(FlightSpecifications.hasId(id));
//		}
//
//		if (StringUtils.hasText(flightNumber))
//			spec = spec.and(FlightSpecifications.hasFlightNumber(flightNumber));
//
//		if (StringUtils.hasText(takeoffIata))
//			spec = spec.and(FlightSpecifications.hasTakoffIata(takeoffIata));
//
//		if (takeoffTime != null)
//			spec = spec.and(FlightSpecifications.hasTakoffTime(takeoffTime));
//
//		return flightRepository.findAll(spec, Sort.by("id"));
//	}
	
	public List<Flight> findFlightsByExample(Flight example) {

		long id = example.getId();
		String flightNumber = example.getFlightNumber();
		String takeoffIata = null;
		Airport takeoff = example.getTakeoff();
		if (takeoff != null)
			takeoffIata = takeoff.getIata();
		LocalDateTime takeoffTime = example.getTakeoffTime();

		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		
		QFlight flight = QFlight.flight;
		if (id > 0) {
			predicates.add(flight.id.eq(id));
		}

		if (StringUtils.hasText(flightNumber))
			predicates.add(flight.flightNumber.startsWithIgnoreCase(flightNumber));

		if (StringUtils.hasText(takeoffIata))
			predicates.add(flight.takeoff.iata.startsWith(takeoffIata));

		if (takeoffTime != null) {
			LocalDateTime startOfDay = LocalDateTime.of(takeoffTime.toLocalDate(), LocalTime.MIDNIGHT);
			predicates.add(flight.takeoffTime.between(startOfDay, startOfDay.plusDays(1)));
		}
			
		return Lists.newArrayList(flightRepository.findAll(ExpressionUtils.allOf(predicates)));
	}

	
//	@Transactional --> hosszú tranzakció, mert a getDelay lassú
//	@Scheduled(cron="*/5 * * * * *")
//	@SchedulerLock(name = "updateDelays")
//	@Async
	public void updateDelays() {
		System.out.println("updateDelays called");
		flightRepository.findAll().forEach(f -> {
			updateFlightWithDelay(f);
		});
	}

	private void updateFlightWithDelay(Flight f) {
		f.setDelayInSec(delayService.getDelay(f.getId()));
		flightRepository.save(f);
	}
	
	
	public void startDelayPollingForFlight(long flightId, long rate) {
		ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(() ->{
			Optional<Flight> flightOptional = flightRepository.findById(flightId);
			if(flightOptional.isPresent())
				updateFlightWithDelay(flightOptional.get());
		}, rate);
		
		stopDelayPollingForFlight(flightId);
		delayPollerJobs.put(flightId, scheduledFuture);
	}
	
	public void stopDelayPollingForFlight(long flightId) {
		ScheduledFuture<?> scheduledFuture = delayPollerJobs.get(flightId);
		if(scheduledFuture != null)
			scheduledFuture.cancel(false);
	}
//	
//	@Scheduled(cron="*/10 * * * * *")
//	public void dummy() {
//		try {
//			Thread.sleep(8000);
//		} catch (InterruptedException e) {
//		}
//		System.out.println("dummy called");
//	}
}
