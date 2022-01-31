package dtu.diatool.models;

public class Observation {

	private Integer hour;
	private Double avgCarbs;
	private Double avgGlucose;
	private Double avgUnitsRapid;
	private Double avgUnitsLong;
	private Integer eventsCarbs;
	private Integer eventsUnitsRapid;
	private Integer eventsUnitsLong;
	
	public Integer getHour() {
		return hour;
	}
	
	public void setHour(Integer hour) {
		this.hour = hour;
	}
	
	public Double getAvgCarbs() {
		return avgCarbs;
	}

	public void setAvgCarbs(Double avgCarbs) {
		this.avgCarbs = avgCarbs;
	}

	public Double getAvgGlucose() {
		return avgGlucose;
	}

	public void setAvgGlucose(Double avgGlucose) {
		this.avgGlucose = avgGlucose;
	}

	public Double getAvgUnitsRapid() {
		return avgUnitsRapid;
	}

	public void setAvgUnitsRapid(Double avgUnitsRapid) {
		this.avgUnitsRapid = avgUnitsRapid;
	}

	public Double getAvgUnitsLong() {
		return avgUnitsLong;
	}

	public void setAvgUnitsLong(Double avgUnitsLong) {
		this.avgUnitsLong = avgUnitsLong;
	}

	public Integer getEventsCarbs() {
		return eventsCarbs;
	}

	public void setEventsCarbs(Integer eventsCarbs) {
		this.eventsCarbs = eventsCarbs;
	}

	public Integer getEventsUnitsRapid() {
		return eventsUnitsRapid;
	}

	public void setEventsUnitsRapid(Integer eventsUnitsRapid) {
		this.eventsUnitsRapid = eventsUnitsRapid;
	}

	public Integer getEventsUnitsLong() {
		return eventsUnitsLong;
	}

	public void setEventsUnitsLong(Integer eventsUnitsLong) {
		this.eventsUnitsLong = eventsUnitsLong;
	}
}
