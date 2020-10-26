package com.prathyusha.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.prathyusha.config.BatchConfig;

//import com.techprimers.springbatchexample1.config.Monitoring;
//import com.techprimers.springbatchexample1.config.SpringBatchConfig;
import javaxt.io.Directory;
import javaxt.io.Directory.Event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/load")
public class PersonController {
	
	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job job;
	
	static Resource[] res;

	@GetMapping
	public BatchStatus load() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, IOException, InterruptedException {
		PersonController lc = new PersonController();
		Map<String, JobParameter> maps = new HashMap<>();
		maps.put("time", new JobParameter(System.currentTimeMillis()));
		JobParameters parameters = new JobParameters(maps);
		JobExecution jobExecution = jobLauncher.run(job, parameters);
		System.out.println("JobExecution: " + jobExecution.getStatus());
		
		Directory folder = new Directory("E:\\csvfiles");
		Directory folderCopy = new Directory("E:\\copyfiles");
		try {
			sync(folder, folderCopy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (jobExecution.isRunning()) {
			System.out.println("...");
		}

		return jobExecution.getStatus();
	}


	@SuppressWarnings("rawtypes")
	private void sync(Directory source, Directory destination) throws Exception {

		PersonController ld = new PersonController();
		List events = source.getEvents();
		while (true) {
			Event event;
			// Wait for new events to be added to the que
			synchronized (events) {
				while (events.isEmpty()) {
					try {
						System.out.println("waiting to do a event");
						events.wait();
						System.out.println("events are waiting");
					} catch (InterruptedException e) {
					}
				}
				event = (Event) events.remove(0);
			}
			int eventID = event.getEventID();
			System.out.println("EventId : " + eventID);
			if (eventID == Event.DELETE) {
				String path = destination + "\\" + event.getFile().substring(source.toString().length());
				System.out.println("path is " + path);
			
				new java.io.File(path).delete();
			} else {
				
				java.io.File obj = new java.io.File(event.getFile());
				if (obj.isDirectory()) {
					javaxt.io.Directory dir = new javaxt.io.Directory(obj);
					javaxt.io.Directory dest = new javaxt.io.Directory(
							destination + dir.toString().substring(source.toString().length()));
					switch (eventID) {
					case (Event.CREATE):
						dir.copyTo(dest, true);
						System.out.println("event creation");
						break;
					case (Event.MODIFY):
						System.out.println("event modification");
						break; // TODO
					case (Event.RENAME): {
						javaxt.io.Directory orgDir = new javaxt.io.Directory(event.getOriginalFile());
						dest = new javaxt.io.Directory(
								destination + orgDir.toString().substring(source.toString().length()));
						dest.rename(dir.getName());
						System.out.println("renaming");
						break;
					}
					}
				} else {
					javaxt.io.File file = new javaxt.io.File(obj);
					javaxt.io.File dest = new javaxt.io.File(
							destination + file.toString().substring(source.toString().length()));

					switch (eventID) {
					case (Event.CREATE):
						event.getFile();
						System.out.println("file name is " + event.getFile());
						PersonController lde = new PersonController();
						Map<String, JobParameter> maps = new HashMap<>();
						maps.put("time2", new JobParameter(System.currentTimeMillis()));
						JobParameters parameters = new JobParameters(maps);
						JobExecution jobExecution = jobLauncher.run(job, parameters);
						System.out.println("JobExecution: " + jobExecution.getStatus().toString());
						System.out.println("Batch is executed succesfully..");
						BatchConfig spc = new BatchConfig();
						ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) spc.taskExecutor();
						System.out.println(
								"value job " + job.getJobParametersIncrementer() + " " + taskExecutor.getActiveCount());
						System.out.println("else part created");
						break;
					case (Event.MODIFY):
						file.copyTo(dest, true);
						break;
					case (Event.RENAME): {
						javaxt.io.File orgFile = new javaxt.io.File(event.getOriginalFile());
						dest = new javaxt.io.File(
								destination + orgFile.toString().substring(source.toString().length()));
						dest.rename(file.getName());
						System.out.println("Renamed else part");
						break;

					}

					}
				}

			}

		}

	}

	
	@RequestMapping("/Fileslist")
	public ModelAndView manualSchedule() {
		File files = new File("E:\\csvfiles");
		File[] listOfFiles = files.listFiles();
		List<String> Names = new ArrayList<>();
		ModelAndView model = new ModelAndView("show");
		for (File file : listOfFiles) {

			Names.add(file.getName());
		}
		model.addObject("ListOfFiles", Names);

		return model;

	}

	@RequestMapping("/manualmode")
	public ModelAndView manualmodeSch(HttpServletRequest request, HttpServletResponse response) {

		Random rannum = new Random();

		String dateTimeLocal = request.getParameter("selecteddate");

		String[] fileNames = request.getParameterValues("names");

		ClassLoader cl = this.getClass().getClassLoader();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
		// Resource[] res = null;
		ModelAndView mdv = new ModelAndView("show");
		res = new Resource[fileNames.length];

		int i = 0;
		try {
			Resource[] resources = resolver.getResources("file:e:/csvfiles/person*.csv");

			for (Resource resource : resources) {

				for (String resource2 : fileNames) {

					if (resource.getFilename().equalsIgnoreCase(resource2)) {
						res[i] = resource;
						i++;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();

		}

		for (Resource resource : res) {
			System.out.println("resorces selected " + resource.getFilename());

			System.out.println("Saving file: " + resource.getFilename());

		}
		TimerTask task = new TimerTask() {

			@Override
			public void run() {

				Map<String, JobParameter> maps = new HashMap<>();
				maps.put("time9", new JobParameter(System.currentTimeMillis()));
				// maps.put("reso", res);
				BatchConfig mtb = new BatchConfig(res);
				System.out.println("exceutes");
				JobParameters parameters = new JobParameters(maps);
				try {
					JobExecution jobExecution = jobLauncher.run(job, parameters);
				} catch (JobExecutionAlreadyRunningException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JobRestartException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JobInstanceAlreadyCompleteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JobParametersInvalidException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		try {
			Date futureDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateTimeLocal);
			System.out.println(futureDate);
			Timer timer = new Timer();
			timer.schedule(task, futureDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mdv;

	}


}
