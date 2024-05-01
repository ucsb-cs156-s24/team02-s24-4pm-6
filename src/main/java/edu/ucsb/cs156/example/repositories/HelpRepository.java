package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Help;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HelpRepository extends CrudRepository<Help, Long> {
  Iterable<Help> findAllByRequesterEmail(String requesterEmail);
}