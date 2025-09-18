package org.example.api.data.repository;

import org.example.api.data.entity.Customer;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
  Optional<Customer> findByEmail(String email);
  void deleteByEmail(String email);

  // Verificar si existe un cliente con el email proporcionado
  boolean existsByEmail(String email);
  Optional<Customer> findByPassword(String password);
  boolean existsById(Integer customerId);
}
