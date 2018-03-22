package childCareJourney;

import java.util.Date;
import java.util.Set;

public class CPP {
	private Integer id;
	private Date startDate;
	private Date endDate;
	private Set<ECPPStatus> status;
	public CPP(Integer id, Date startDate, Date endDate, Set<ECPPStatus> status) {
		this.id = id;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
	}
	public Integer getId() {
		return id;
	}
	public Date getStartDate() {
		return startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public Set<ECPPStatus> getStatus() {
		return status;
	}
}
