package dtu.diatool.models;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Dataset {

	private String uuid;
	private String name;
	private Date startDate;
	private Date endDate;
	private Date uploadDate;
	
}
