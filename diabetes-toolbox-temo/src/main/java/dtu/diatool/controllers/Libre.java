package dtu.diatool.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import dtu.diatool.models.Dataset;
import dtu.diatool.models.Observation;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin
public class Libre {

	public static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/diatool";
	public static Connection conn;
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	static {
		System.out.println("Connecting to MySQL...");
		try {
			conn = DriverManager.getConnection(CONNECTION_URL, "diatool", "");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@PutMapping("/upload-libre-data")
	public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws SQLException, CsvValidationException, IOException, ParseException {
		
		Date firstDate = new Date();
		Date lastDate = new Date(0);
		HashMap<Date, HashMap<String, Double>> m = new HashMap<Date, HashMap<String, Double>>();
		Reader reader = new InputStreamReader(file.getInputStream());
		CSVReader csvReader = new CSVReader(reader);
		String[] nextRecord;
		int lines = 0;
		while ((nextRecord = csvReader.readNext()) != null) {
			lines++;
			if (lines <= 2) continue;
			
			Date date = formatter.parse(nextRecord[2]);
			if (!m.containsKey(date)) {
				m.put(date, new HashMap<String, Double>());
			}
			if (!"".equals(nextRecord[4])) {
				m.get(date).put("glucose", Double.valueOf(nextRecord[4]));
			}
			if (!"".equals(nextRecord[5])) {
				m.get(date).put("glucose", Double.valueOf(nextRecord[5]));
				m.get(date).put("scan", Double.valueOf(nextRecord[5]));
			}
			if (!"".equals(nextRecord[7])) {
				m.get(date).put("units_rapid", Double.valueOf(nextRecord[7]));
			}
			if (!"".equals(nextRecord[9])) {
				m.get(date).put("carbs", Double.valueOf(nextRecord[9]));
			}
			if (!"".equals(nextRecord[12])) {
				m.get(date).put("units_long", Double.valueOf(nextRecord[12]));
			}
			
			if (date.compareTo(firstDate) < 0) { // date < first date
				firstDate = date;
			}
			if (date.compareTo(lastDate) > 0) { // last date < date
				lastDate = date;
			}
		}
		csvReader.close();
		
		String filename = file.getOriginalFilename();
		Integer fileId = executeQueryAndReturnId("insert into files(uuid, filename, start_date, end_date) values (uuid(), ?, ?, ?)",
				filename.substring(filename.lastIndexOf(File.separator) + 1),
				firstDate,
				lastDate);
		
		for (Date date : m.keySet()) {
			if (m.get(date).size() > 0) {
				executeQueryAndReturnId("insert into readings(id_file, date, glucose, carbs, units_rapid, units_long, scan) values (?, ?, ?, ?, ?, ?, ?)",
					fileId,
					date,
					m.get(date).get("glucose"),
					m.get(date).get("carbs"),
					m.get(date).get("units_rapid"),
					m.get(date).get("units_long"),
					m.get(date).get("scan"));
			}
		}
		
		String uuid = executeQueryAndReturnFirstString("select uuid from files where id = ? order by upload_date desc", fileId);
		return ResponseEntity.ok(uuid);
	}
	
	@GetMapping("/data-available")
	public ResponseEntity<List<Dataset>> getDatesets() throws SQLException {
		List<Dataset> datasets = new LinkedList<Dataset>();
		PreparedStatement stmt =  conn.prepareStatement("select * from files");
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			Dataset d = new Dataset();
			d.setUuid(rs.getString("uuid"));
			d.setName(rs.getString("filename"));
			d.setStartDate(rs.getTimestamp("start_date"));
			d.setEndDate(rs.getTimestamp("end_date"));
			d.setUploadDate(rs.getTimestamp("upload_date"));
			datasets.add(d);
		}
		return ResponseEntity.ok(datasets);
	}
	
	@DeleteMapping("delete/{id}")
	public ResponseEntity<Boolean> delete(@PathVariable String id) throws SQLException {
		PreparedStatement stmt =  conn.prepareStatement("delete from files where uuid = ?");
		stmt.setString(1, id);
		boolean result = stmt.execute();
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("/{id}/daily/{days}")
	public ResponseEntity<List<Observation>> getDailyData(@PathVariable String id, @PathVariable List<String> days) throws SQLException {
		String fileId = executeQueryAndReturnFirstString("select id from files where uuid = ?", id);
		String dow = StringUtils.join(days,",");
		
		String baseHour = "SELECT %h AS hour, "
				+ "AVG(glucose) AS avg_glucose, "
				+ "AVG(carbs) AS avg_carbs, "
				+ "COUNT(carbs) AS events_carbs, "
				+ "AVG(units_rapid) AS avg_units_rapid, "
				+ "COUNT(units_rapid) AS events_units_rapid, "
				+ "AVG(units_long) AS avg_units_long, "
				+ "COUNT(units_long) AS events_units_long "
				+ "FROM readings WHERE id_file = %id AND HOUR(`DATE`) = %h AND DAYOFWEEK(`DATE`) IN (%dow) ";
		String sql = "";
		for (int i = 0; i < 24; i++) {
			String newHour = baseHour.replace("%h", String.valueOf(i));
			newHour = newHour.replace("%id", fileId);
			newHour = newHour.replace("%dow", dow);
			sql += newHour;
			if (i < 23) {
				sql += " UNION ";
			}
		}
		
		List<Observation> obs = new LinkedList<Observation>();
		PreparedStatement stmt =  conn.prepareStatement(sql);
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		while(rs.next()) {
			Observation o = new Observation();
			o.setHour(rs.getInt("hour"));
			o.setAvgCarbs(rs.getDouble("avg_carbs"));
			o.setAvgGlucose(rs.getDouble("avg_glucose"));
			o.setAvgUnitsLong(rs.getDouble("avg_units_long"));
			o.setAvgUnitsRapid(rs.getDouble("avg_units_rapid"));
			o.setEventsCarbs(rs.getInt("events_carbs"));
			o.setEventsUnitsLong(rs.getInt("events_units_long"));
			o.setEventsUnitsRapid(rs.getInt("events_units_rapid"));
			obs.add(o);
		}
		
		return ResponseEntity.ok(obs);
	}
	
	private static String executeQueryAndReturnFirstString(String sql, Object...args) throws SQLException {
		PreparedStatement stmt =  conn.prepareStatement(sql);
		int i = 1;
		for (Object arg : args) {
			if (arg instanceof String) {
				stmt.setString(i, (String) arg);
			} else if (arg instanceof Double) {
				stmt.setDouble(i, (Double) arg);
			} else if (arg instanceof Integer) {
				stmt.setInt(i, (Integer) arg);
			} else if (arg instanceof Date) {
				stmt.setTimestamp(i, new java.sql.Timestamp(((Date) arg).getTime()));
			} else {
				stmt.setNull(i, java.sql.Types.NULL);
			}
			i++;
		}
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		if (rs.next()) {
			return rs.getString(1);
		}
		return null;
	}
	
	private static int executeQueryAndReturnId(String sql, Object... args) throws SQLException {
		PreparedStatement stmt =  conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int i = 1;
		for (Object arg : args) {
			if (arg instanceof String) {
				stmt.setString(i, (String) arg);
			} else if (arg instanceof Double) {
				stmt.setDouble(i, (Double) arg);
			} else if (arg instanceof Integer) {
				stmt.setInt(i, (Integer) arg);
			} else if (arg instanceof Date) {
				stmt.setTimestamp(i, new java.sql.Timestamp(((Date) arg).getTime()));
			} else {
				stmt.setNull(i, java.sql.Types.NULL);
			}
			i++;
		}
		stmt.execute();
		ResultSet rs = stmt.getGeneratedKeys();
		if (rs.next()) {
			return rs.getInt(1);
		}
		return -1;
	}
}
