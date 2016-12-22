package dk.os2opgavefordeler.orgunit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages whether a job is currently ongoing for a municipality
 * @author hlo@miracle.dk
 */
@Singleton
public class OrgUpdateManager {
	private Set<Long> runningJobs;

  @PostConstruct
	void init(){
		runningJobs = new HashSet<>();
	}

	/**
	 * Checks whether a job is already ongoing for a municipality and starts it if not
	 * @param municipalityId id for municipality that import needs to run for.
	 * @return true if job can start
	 */
  public boolean importJobAllowedFor(Long municipalityId){
		if(runningJobs.contains(municipalityId)){
			return false;
		} else {
			runningJobs.add(municipalityId);
			return true;
		}
	}

	/**
	 * Removes the municipalityId from runningJobs, allowing another to begin.
	 * @param municipalityId id for municipality for which an import has stopped.
	 */
  public void endJob(Long municipalityId){
		runningJobs.remove(municipalityId);
	}
}


