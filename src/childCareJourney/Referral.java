package childCareJourney;

import java.util.Date;

public class Referral {
	
	private Integer id;
	private Integer ageAtReferral;
	private EGender gender;
	private EEthnicity ethnicity;
	private EWard ward;
	private Date referralDate;

	public Referral(Integer id, Integer age, EGender gender, EEthnicity ethnicity, EWard ward, Date date) {
		this.id = id;
		this.ageAtReferral = age;
		this.gender = gender;
		this.ethnicity = ethnicity;
		this.ward = ward;
		this.referralDate = date;
	}

	public Integer getId() {
		return id;
	}

	public Integer getAgeAtReferral() {
		return ageAtReferral;
	}

	public EGender getGender() {
		return gender;
	}

	public EEthnicity getEthnicity() {
		return ethnicity;
	}

	public EWard getWard() {
		return ward;
	}

	public Date getReferralDate() {
		return referralDate;
	}
}
