package peeling.project.basic.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;
import peeling.project.basic.domain.log.Log;

public interface LogRepository extends JpaRepository<Log , Long> {


}
