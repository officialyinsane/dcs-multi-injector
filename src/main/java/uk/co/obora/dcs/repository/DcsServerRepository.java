package uk.co.obora.dcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.obora.dcs.entity.DcsServer;

public interface DcsServerRepository extends JpaRepository<DcsServer, Long> {
}
