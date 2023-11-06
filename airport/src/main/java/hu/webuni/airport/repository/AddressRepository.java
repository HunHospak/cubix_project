package hu.webuni.airport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hu.webuni.airport.aspect.LogCall;
import hu.webuni.airport.model.Address;

@LogCall
public interface AddressRepository extends JpaRepository<Address, Long>{
	
}
