package dtu.diatool.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin
public class Processor {

	@Autowired
	private Environment env;
	
	@PutMapping("/upload")
	public ResponseEntity<UUID> upload(@RequestParam("file") MultipartFile file) {
		UUID id = UUID.randomUUID();
		String dir = "files" + File.separator + id.toString() + File.separator;
		
		// copy the file
//		File dataFile = null;
//		try {
//			new File(dir).mkdir();
//			InputStream is = file.getInputStream();
//			dataFile = new File(dir + File.separator + "data");
//			FileUtils.copyInputStreamToFile(is, dataFile);
//		} catch(IOException e) {
//			e.printStackTrace();
//			return ResponseEntity.internalServerError().build();
//		}
		
		// process the data
//		File imgFile1 = new File(dir + File.separator + "pic1.png");
//		File imgFile2 = new File(dir + File.separator + "pic2.png");
		
		// prepare command
//		try {
//			System.out.println("start");
//			Runtime run  = Runtime.getRuntime();
//			run.exec();
//
//			System.out.println("done");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		try {
//			FileUtils.copyInputStreamToFile(new FileInputStream("C:\\Users\\andbur\\Desktop\\img1.jpg"), imgFile1);
//			FileUtils.copyInputStreamToFile(new FileInputStream("C:\\Users\\andbur\\Desktop\\img2.jpg"), imgFile2);
//		} catch (IOException e) {
//			e.printStackTrace();
//			return ResponseEntity.internalServerError().build();
//		}
		
		
		return ResponseEntity.ok(id);
	}
	
	@GetMapping(
		value = "/{id}/pic1",
		produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] getPicture1(@PathVariable String id) {
		File f = new File("files" + File.separator + id + File.separator + "pic1.jpg");
//		if (f.exists()) {
			try {
				return IOUtils.toByteArray(getClass().getResourceAsStream("/pic1.png"));
//				return IOUtils.toByteArray(new FileInputStream(f));
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}
		return null;
	}
	
	@GetMapping(
		value = "/{id}/pic2",
		produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] getPicture2(@PathVariable String id) {
		File f = new File("files" + File.separator + id + File.separator + "pic2.jpg");
//		if (f.exists()) {
			try {
				return IOUtils.toByteArray(getClass().getResourceAsStream("/pic2.png"));
//				return IOUtils.toByteArray(new FileInputStream(f));
			} catch (IOException e) {
				e.printStackTrace();
			}
//		}
		return null;
	}
}
