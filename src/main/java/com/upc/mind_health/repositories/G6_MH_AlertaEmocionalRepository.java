package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_AlertaEmocional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface G6_MH_AlertaEmocionalRepository extends JpaRepository<G6_MH_AlertaEmocional, Long> {
}