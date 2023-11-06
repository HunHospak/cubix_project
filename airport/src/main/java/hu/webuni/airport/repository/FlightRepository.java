package hu.webuni.airport.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import hu.webuni.airport.model.Flight;
import hu.webuni.airport.model.QFlight;

public interface FlightRepository extends JpaRepository<Flight, Long>,
		JpaSpecificationExecutor<Flight>,
		QuerydslPredicateExecutor<Flight>,
		QuerydslBinderCustomizer<QFlight>{
	
	
	@Override
	default void customize(QuerydslBindings bindings, QFlight flight) {
		bindings.bind(flight.flightNumber).first((path, value) -> path.startsWithIgnoreCase(value));
		bindings.bind(flight.takeoff.iata).first((path, value) -> path.startsWith(value));
		
//		bindings.bind(flight.takeoffTime).first((path, value) -> {
//			LocalDateTime startOfDay = LocalDateTime.of(value.toLocalDate(), LocalTime.MIDNIGHT);
//			return path.between(startOfDay, startOfDay.plusDays(1));
//		});
		bindings.bind(flight.takeoffTime).all((path, values) -> {
			if(values.size() !=2 )
				return Optional.empty();
			
			Iterator<? extends LocalDateTime> iterator = values.iterator();
			LocalDateTime start = iterator.next();
			LocalDateTime end = iterator.next();
			LocalDateTime startOfDay = LocalDateTime.of(start.toLocalDate(), LocalTime.MIDNIGHT);
			LocalDateTime endOfDay = LocalDateTime.of(end.toLocalDate(), LocalTime.MIDNIGHT).plusDays(1);
			
			return Optional.of(path.between(startOfDay, endOfDay));
			
		});
	}
}
